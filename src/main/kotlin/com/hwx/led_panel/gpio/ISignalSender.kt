package com.hwx.led_panel.gpio

import com.github.mbelling.ws281x.LedStripType
import com.github.mbelling.ws281x.Ws281xLedStrip
import com.hwx.led_panel.Config
import org.springframework.stereotype.Component

interface ISignalSender {
    fun push()
}

@Component
class SignalSender(
    private val holder: ILedStripsHolder,
) : ISignalSender {

    private val ws281xLedStrip = Ws281xLedStrip(
        Config.LED_CNT * Config.LED_STRIPS_CNT,  // leds
        Config.PIN_NUMBER,  // Using pin 10 to do SPI, which should allow non-sudo access
        800000,  // freq hz
        10,  // dma
        255,  // brightness
        Config.PIN_CHANNEL,  // pwm channel
        false,  // invert
        LedStripType.WS2811_STRIP_RGB,  // Strip type
        false // clear on exit
    )

    override fun push() {
        holder.strips.forEachIndexed { index, ledStrip ->
            val shift = index * Config.LED_CNT

            //fade step
            for (i in 0 until ledStrip.list.size) {
                val pixel = ledStrip.list[i]
                ws281xLedStrip.setPixel(
                    shift + i,
                    pixel.red,
                    pixel.green,
                    pixel.blue,
                )
            }
        }
        ws281xLedStrip.render()
    }
}