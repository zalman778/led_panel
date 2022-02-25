package com.hwx.led_panel.service

import com.hwx.led_panel.capture.ISoundRunnable
import com.hwx.led_panel.capture.ISoundScanner
import com.hwx.led_panel.effect.IFirstEffect
import com.hwx.led_panel.gpio.ISignalSender
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class LedService(
    @Autowired val soundRunnable: ISoundRunnable,
    @Autowired val scanner: ISoundScanner,
    @Autowired val firstEffect: IFirstEffect,
    @Autowired val sender: ISignalSender,
) {

    private var listenJob: Job? = null

    init {
        listen()
        soundRunnable.init()
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