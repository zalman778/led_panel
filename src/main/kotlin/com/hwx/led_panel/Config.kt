package com.hwx.led_panel

import org.springframework.context.ApplicationContext
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.lang.Double.MIN_VALUE
import java.util.*

object Config {

    var LED_STRIPS_CNT = 60
    var LED_CNT = 29
    var PIN_NUMBER = 13
    var PIN_CHANNEL = 1

    //максимальная частота
    var MAX_HZ = 15000

    //процент громкости, чтобы отобразить бар звука
    var MIN_SOUND_AMPL_FILTER_PRC = 3.2f

    //множители аплтуд для частот в диапазонах
    var LOW_HZ_MLT = 0.02f
    var MID_HZ_MLT_LOW_BOUND = 1500f
    var MID_HZ_MLT = 1.0f
    var MID_HZ_MLT_HIGH_BOUND = 6000f
    var HIGH_HZ_MLT = 1.0f

    //сколько последних хранить для сравнения
    var MAX_LAST_AMPL_LIST_SIZE = 500
    var MIN_HSB_VAL = 220
    var MAX_HSB_VAL = 360
    var LED_BRIGHT = 0.71f
    var ASYM_MODE = 1
    var SEND = "1"

    fun readAll(ac: ApplicationContext) {
        val prop = Properties()
        var inp: InputStream? = null
        try {
            val resource = ac.getResource("classpath:Config.properties")
            inp = resource.inputStream

            //inp = new FileInputStream("Config.properties");
            prop.load(inp)
            LED_STRIPS_CNT = prop.getProperty("INT_CNT").toInt()
            LED_CNT = prop.getProperty("LED_CNT").toInt()
            MAX_HZ = prop.getProperty("MAX_HZ").toInt()
            //MAX_HZ_LIMIT = Integer.parseInt(prop.getProperty("MAX_HZ_LIMIT"));
            MIN_SOUND_AMPL_FILTER_PRC = prop.getProperty("MIN_SOUAND_AMPL_FILTER_PRC").toFloat()
            LOW_HZ_MLT = prop.getProperty("LOW_HZ_MLT").toFloat()
            //LOW_HZ_MLT_HIGH_BOUND = Float.parseFloat(prop.getProperty("LOW_HZ_MLT_HIGH_BOUND"));
            MID_HZ_MLT = prop.getProperty("MID_HZ_MLT").toFloat()
            MID_HZ_MLT_LOW_BOUND = prop.getProperty("MID_HZ_MLT_LOW_BOUND").toFloat()
            MID_HZ_MLT_HIGH_BOUND = prop.getProperty("MID_HZ_MLT_HIGH_BOUND").toFloat()
            HIGH_HZ_MLT = prop.getProperty("HIGH_HZ_MLT").toFloat()
            //HIGH_HZ_MLT_LOW_BOUND = Float.parseFloat(prop.getProperty("HIGH_HZ_MLT_LOW_BOUND"));
            MAX_LAST_AMPL_LIST_SIZE = prop.getProperty("MAX_LAST_AMPL_LIST_SIZE").toInt()
            MIN_HSB_VAL = prop.getProperty("MIN_HSB_VAL").toInt()
            MAX_HSB_VAL = prop.getProperty("MAX_HSB_VAL").toInt()
            LED_BRIGHT = prop.getProperty("LED_BRIGHT").toFloat()
            ASYM_MODE = prop.getProperty("ASYM_MODE").toInt()
            SEND = prop.getProperty("SEND")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun saveAll() {
        val prop = Properties()
        var output: OutputStream? = null
        try {
            output = FileOutputStream("Config.properties")

            // set the properties value
            prop.setProperty("INT_CNT", LED_STRIPS_CNT.toString())
            prop.setProperty("LED_CNT", LED_CNT.toString())
            prop.setProperty("MAX_HZ", MAX_HZ.toString())
            //prop.setProperty("MAX_HZ_LIMIT", String.valueOf(MAX_HZ_LIMIT));
            prop.setProperty("MIN_SOUAND_AMPL_FILTER_PRC", MIN_SOUND_AMPL_FILTER_PRC.toString())
            prop.setProperty("LOW_HZ_MLT", LOW_HZ_MLT.toString())
            //prop.setProperty("LOW_HZ_MLT_HIGH_BOUND", String.valueOf(LOW_HZ_MLT_HIGH_BOUND));
            prop.setProperty("MID_HZ_MLT", MID_HZ_MLT.toString())
            prop.setProperty("MID_HZ_MLT_LOW_BOUND", MID_HZ_MLT_LOW_BOUND.toString())
            prop.setProperty("MID_HZ_MLT_HIGH_BOUND", MID_HZ_MLT_HIGH_BOUND.toString())
            prop.setProperty("HIGH_HZ_MLT", HIGH_HZ_MLT.toString())
            //prop.setProperty("HIGH_HZ_MLT_LOW_BOUND", String.valueOf(HIGH_HZ_MLT_LOW_BOUND));
            prop.setProperty("MAX_LAST_AMPL_LIST_SIZE", MAX_LAST_AMPL_LIST_SIZE.toString())
            prop.setProperty("MIN_HSB_VAL", MIN_HSB_VAL.toString())
            prop.setProperty("MAX_HSB_VAL", MAX_HSB_VAL.toString())
            prop.setProperty("LED_BRIGHT", LED_BRIGHT.toString())
            prop.setProperty("ASYM_MODE", ASYM_MODE.toString())
            prop.setProperty("SEND", SEND)

            // save properties to project root folder
            prop.store(output, null)
        } catch (io: IOException) {
            io.printStackTrace()
        } finally {
            if (output != null) {
                try {
                    output.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }
}
