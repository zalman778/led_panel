package com.hwx.led_panel.effect

import com.hwx.led_panel.Config
import com.hwx.led_panel.capture.ISoundScanner
import com.hwx.led_panel.gpio.ILedStripsHolder
import com.hwx.led_panel.model.Led
import org.springframework.stereotype.Component
import java.awt.Color
import java.util.*
import kotlin.math.roundToInt

interface IFirstEffect {
    fun reset()
    fun tick(data: ISoundScanner.Result)
}

@Component
class FirstEffect(
    private val holder: ILedStripsHolder,
) : IFirstEffect {

    private val intMaxAmplsArr = DoubleArray(Config.LED_STRIPS_CNT)
    private val hsbArr = ArrayList<Float>(Config.LED_STRIPS_CNT - 1)//Config.LED_STRIPS_CNT - 1)
    private val silenceArr =
        ArrayList<Int>(Config.LED_STRIPS_CNT - 1)//Config.LED_STRIPS_CNT - 1) //массив тишины- - выключаем свет, если нет
    private val maxAmplArrAllTime = ArrayList<Double>(Config.LED_STRIPS_CNT)
    private var currVal = 0f
    private var currentMaxAmplVal = -Double.MIN_VALUE //текущее значение макс громкости, для подсчета процена (выше)
    private var diffMinMax = 0f
    private var additionalDiff = 0f

    @get:Synchronized
    private val lastXAmplList = LinkedList<Double>()

    override fun reset() {
        maxAmplArrAllTime.clear()
        lastXAmplList.clear()
        currentMaxAmplVal = -Double.MIN_VALUE
    }

    override fun tick(data: ISoundScanner.Result) {
        val maxHz = data.maxHz
        val minHz = data.minHz
        val hzAmplArr = data.hzAmplArr

        ///System.out.println("inside tick:"+maxHz);
        //cutting min-max to LED_STRIPS_CNT intervals

        ///System.out.println("inside tick:"+maxHz);
        //cutting min-max to LED_STRIPS_CNT intervals
        val deltaInt = ((maxHz - minHz) / Config.LED_STRIPS_CNT).roundToInt()

        //calculation HSB-color limits from Cfg

        //calculation HSB-color limits from Cfg
        val min_hsb_val_in_1: Float = Config.MIN_HSB_VAL / 360.0f
        val max_hsb_val_in_1: Float = Config.MAX_HSB_VAL / 360.0f

        //array of max Ampl for each Interval
        Arrays.fill(intMaxAmplsArr, 0.0)

        for (i in 0 until Config.LED_STRIPS_CNT - 1) {
            var maxCurrAmpl = 0.0
            for (j in hzAmplArr.indices) {
                if (hzAmplArr[j][0] > i * deltaInt && hzAmplArr[j][0] < (i + 1) * deltaInt) {
                    if (hzAmplArr[j][1] > maxCurrAmpl) {
                        maxCurrAmpl = hzAmplArr[j][1]
                    }
                }
            }
            intMaxAmplsArr[i] = maxCurrAmpl

            //update all-time peaks;
            if (maxAmplArrAllTime[i] < intMaxAmplsArr[i]) {
                maxAmplArrAllTime[i] = intMaxAmplsArr[i]
            }
        }

        //для каждого интервала делаем расчет
        for (i in 0 until Config.LED_STRIPS_CNT - 1) {
            currVal = (intMaxAmplsArr[i] / maxAmplArrAllTime[i]) as Float

            //Список последних аплитуд и сравнение со средним
            //если  не полный, то замолняем
            if (lastXAmplList.size < Config.MAX_LAST_AMPL_LIST_SIZE) {
                lastXAmplList.addFirst(intMaxAmplsArr[i])
            } else {
                //если полный, то удаляем конец, добавляем голову
                if (lastXAmplList.size > 0) {
                    lastXAmplList.removeLast()
                }
                lastXAmplList.addFirst(intMaxAmplsArr[i])

                //проверка всего списка на макс, если меньше, то обновляем макс
                var wasNoMaxInList = true
                //System.out.println("compare:");
                for (x in lastXAmplList) {
                    //System.out.print(x+" ");
                    if (x > Config.MIN_SOUND_AMPL_FILTER_PRC * currentMaxAmplVal / 100) {
                        wasNoMaxInList = false
                        break
                    }
                }
                //System.out.println();
                //System.out.println("was comparing with "+(MIN_SOUND_AMPL_FILTER_PRC * currentMaxAmplVal));
                if (wasNoMaxInList) {
                    reset()
                    //System.out.println("reset");
                }
            }


            //Обновление макс аплитуды
            if (intMaxAmplsArr[i] > currentMaxAmplVal) {
                currentMaxAmplVal = intMaxAmplsArr[i]
            }
            if (intMaxAmplsArr[i] > currentMaxAmplVal * Config.MIN_SOUND_AMPL_FILTER_PRC / 100) {
                silenceArr[i] = 1
            } else {
                silenceArr[i] = 0
            }
            diffMinMax = max_hsb_val_in_1 - min_hsb_val_in_1
            additionalDiff = diffMinMax * currVal
            currVal = min_hsb_val_in_1 + additionalDiff
            hsbArr[i] = currVal
        }


        //writing all HSB int to Led arrays
        val ledIntDelta: Int = Config.LED_CNT / Config.LED_STRIPS_CNT

        for (i in 0 until Config.LED_STRIPS_CNT - 1) {
            //default light, if current sound peak didnt reach its max val
            var red = 1
            var green = 1
            var blue = 1
            if (silenceArr[i] == 1) {
                val rgb = Color.HSBtoRGB(hsbArr[i], 1.0f, 1.0f)
                red = rgb shr 16 and 0xFF
                green = rgb shr 8 and 0xFF
                blue = rgb and 0xFF
            }

            for (j in i * ledIntDelta until (i + 1) * ledIntDelta) {
                holder.strips.getOrNull(j)?.set(j, Led(red, green, blue, Config.LED_BRIGHT))
            }
        }
    }
}