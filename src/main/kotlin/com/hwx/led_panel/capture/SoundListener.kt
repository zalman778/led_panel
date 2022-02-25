package com.hwx.led_panel.capture

import org.springframework.stereotype.Component

interface ISoundRunnable : Runnable {
    fun init()
}

@Component
class SoundListener(
    private val scanner: ISoundScanner,
) : ISoundRunnable {

    private var currentThread: Thread? = null

    override fun init() {
        currentThread = Thread(this)
        currentThread?.start()
    }


    override fun run() {
        scanner.run()
    }
}