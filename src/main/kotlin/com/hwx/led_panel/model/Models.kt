package com.hwx.led_panel.model

import com.hwx.led_panel.Config
import java.util.*

data class Led(
    val red: Int,
    val green: Int,
    val blue: Int,
    val brightness: Float
)

class LedStrip {
    val list = ArrayList<Led>()

     init {
        for (i in 0 until Config.LED_CNT) {
            list.add(Led(10, 10, 10, 0.61f))
        }
    }

    @Synchronized
    operator fun set(i: Int, led: Led) {
        list[i] = led
    }
}