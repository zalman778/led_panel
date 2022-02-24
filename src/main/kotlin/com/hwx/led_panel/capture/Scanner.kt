package com.hwx.led_panel.capture


import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.jvm.JVMAudioInputStream
import be.tarsos.dsp.util.fft.FFT
import com.hwx.led_panel.Config
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.*
import javax.sound.sampled.*

interface ISoundScanner {
    fun init()
    fun run()
    fun clear()

    val data: Flow<Result>

    data class Result(
        val hzAmplArr: List<List<Double>>,
        val maxHz: Double,
        val minHz: Double
    )
}

/**
 * Created by Hiwoo on 06.02.2018.
 */

@Component
class Scanner : ISoundScanner {

    private val bufferSize = 4096
    private val fftSize = bufferSize / 2
    private val sampleRate = 48000

    //массив: частота - амплитуда
    //private Double[][] hzAmplArr = new Double[fftSize][2];
    private val hzAmplArr = ArrayList<ArrayList<Double>>()
    private var maxHz = 0.0
    private var minHz = 0.0

    private var audioProcessor = object : AudioProcessor {
        override fun process(event: AudioEvent) = onProcessorEvent(event)
        override fun processingFinished() = Unit
    }

    private val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, true)
    private val line = AudioSystem.getTargetDataLine(format)
    private val stream = AudioInputStream(line)
    private val audioStream = JVMAudioInputStream(stream)
    private val audioDispatcher = AudioDispatcher(audioStream, bufferSize, 0)

    private val fft = FFT(bufferSize)
    private val amplitudes = FloatArray(fftSize)

    override val data = MutableSharedFlow<ISoundScanner.Result>(1)

    override fun run() {
        audioDispatcher.run()
    }

    private fun onProcessorEvent(audioEvent: AudioEvent): Boolean {
        //System.out.println("new loop of process:");
        val audioBuffer = audioEvent.floatBuffer
        fft.forwardTransform(audioBuffer)
        fft.modulus(audioBuffer, amplitudes)


        //creating array of Hz - Ampl value
        //hzAmplArr = new Double[amplitudes.length][2];
        //hzAmplArr = new ArrayList<>(Collections.nCopies(amplitudes.length, new ArrayList<>()));
        hzAmplArr.clear()
        for (i in amplitudes.indices) {
            hzAmplArr.add(ArrayList())
        }
        minHz = Config.MAX_HZ.toDouble()
        maxHz = 0.0
        for (i in amplitudes.indices) {

            //System.out.printf("Amplitude at %3d Hz: %8.3f     ", (int) fft.binToHz(i, sampleRate) , amplitudes[i]);
            //получаем частоту
            //hzAmplArr[i][0] = fft.binToHz(i, sampleRate);
            hzAmplArr[i].add(fft.binToHz(i, sampleRate.toFloat()))

            //специальное занижение ампитуды частоты (сабик громкий)
            if (hzAmplArr[i][0] < Config.MID_HZ_MLT_LOW_BOUND) //hzAmplArr[i][1] = (double) (amplitudes[i] * Config.LOW_HZ_MLT);
                hzAmplArr[i].add((amplitudes[i] * Config.LOW_HZ_MLT) as Double) else if (hzAmplArr[i][0] >= Config.MID_HZ_MLT_LOW_BOUND
                && hzAmplArr[i][0] < Config.MID_HZ_MLT_HIGH_BOUND
            ) //hzAmplArr[i][1] = (double) amplitudes[i] * Config.MID_HZ_MLT;
                hzAmplArr[i].add(amplitudes[i].toDouble() * Config.MID_HZ_MLT) else  //hzAmplArr[i][1] = (double) amplitudes[i] * Config.HIGH_HZ_MLT;
                hzAmplArr[i].add(amplitudes[i].toDouble() * Config.HIGH_HZ_MLT)


            //min and max
            if (minHz > hzAmplArr[i][0]) minHz = hzAmplArr[i][0]
            if (maxHz < hzAmplArr[i][0]) maxHz = hzAmplArr[i][0]
        }
        //System.out.println(minHz+" "+maxHz);
        //limiting maxHz
        if (maxHz > Config.MAX_HZ) maxHz = Config.MAX_HZ.toDouble()

        val result = ISoundScanner.Result(hzAmplArr.toList(), maxHz, minHz)
        data.tryEmit(result)

        return true
    }

    override fun clear() {
        //cleaning previous
        try {
            audioDispatcher.stop()
            audioStream.close()
            stream.close()
            line.close()
            System.gc()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun init() {
        clear()
        try {
            line.open(format, bufferSize)
            line.start()
            audioDispatcher.addAudioProcessor(audioProcessor)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}