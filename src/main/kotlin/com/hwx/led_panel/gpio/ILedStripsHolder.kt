package com.hwx.led_panel.gpio

import com.hwx.led_panel.Config
import com.hwx.led_panel.model.LedStrip
import org.springframework.stereotype.Component

interface ILedStripsHolder {
    val strips: List<LedStrip>
}

@Component
class LedStripsHolder: ILedStripsHolder {
    override val strips = ArrayList<LedStrip>(Config.LED_STRIPS_CNT)
}