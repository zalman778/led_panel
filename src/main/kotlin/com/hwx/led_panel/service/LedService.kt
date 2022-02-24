package com.hwx.led_panel.service

import com.hwx.led_panel.capture.ISoundScanner
import com.hwx.led_panel.effect.IFirstEffect
import com.hwx.led_panel.gpio.ISignalSender
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LedService(
    @Autowired val scanner: ISoundScanner,
    @Autowired val firstEffect: IFirstEffect,
    @Autowired val sender: ISignalSender,
) {

    private var listenJob: Job? = null

    init {
        listen()
        scanner.init()
        scanner.run()
    }

    private fun listen() {
        listenJob = GlobalScope.launch {
            scanner.data.collect {
                firstEffect.tick(it)
                sender.push()
            }
        }
    }
}