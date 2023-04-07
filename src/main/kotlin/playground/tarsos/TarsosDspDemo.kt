package playground.tarsos

import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchProcessor
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.color.ColorXSVa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.shapes.grid
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.math.saturate
import playground.tarsos.Demos.*
import playground.tarsos.processors.MyPitchProcessor
import playground.tarsos.processors.MyPitchProcessor.Companion.HIGHEST_PITCH
import playground.tarsos.processors.MyPitchProcessor.Companion.LOWEST_PITCH
import playground.tarsos.processors.MyPitchProcessor.Companion.PITCH_RANGE
import playground.tarsos.processors.VolumeProcessor

/**
 * Demo on how to use TarsosDSP to analyze audio input.
 * Uses a 2x3 grid of rectangles, where each rectangle holds one showcase.
 *
 * Showcases:
 * - Volume detection
 * - Volume dynamic range
 * - Volume flash
 * - Pitch detection
 * - Spectrum
 * - Percussion flash
 *
 * Press ESC to terminate. (ESC = Clean shutdown. Other shutdown methods might lag.)
 */
fun main() = application {
    configure {
        width = 640
        height = 640 * 2 / 3 + 20
        position = IntVector2(0, 0)
        title = "OPENRNDR ~ TarsosDSP Demo"
    }
    program {
        // Tarsos setup for volume detection

        // Get an audio stream from the microphone
        // Note: No overlap, because fresh audio buffers are fine
        val sampleRate = 44100f
        val bufferSize = 1024
        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(bufferSize, 0)

        // Amount of audio events buffered for each demo
        val eventBufferSize = 40

        // Processors
        val volumeProcessor = VolumeProcessor(eventBufferSize).also { dispatcher.addAudioProcessor(it) }
        val pitchAlgo = PitchProcessor.PitchEstimationAlgorithm.FFT_YIN
        val pitchProcessor = MyPitchProcessor(pitchAlgo, sampleRate, bufferSize, eventBufferSize)
            .also { dispatcher.addAudioProcessor(it) }

        // Start the dispatching process
        Thread(dispatcher).start()

        // Demo setup
        val rects = drawer.bounds
            .grid(3, 2, marginX = 30.0, marginY = 40.0, gutterX = 5.0, gutterY = 30.0).flatten()

        val w = rects[0].width
        val h = rects[0].height

        // Define general demo draw procedure
        fun Drawer.drawDemo(demo: Demos, content: Drawer.() -> Unit ) {
            val rect = rects[demo.ordinal]

            this.isolated {
                // Draw box
                stroke = ColorRGBa.WHITE
                fill = null
                rectangle(rect)

                // Content
                fill = ColorRGBa.WHITE
                content()

                // Draw demo name
                fill = ColorRGBa.WHITE
                text(demo.trivial, rect.corner + Vector2(0.0, h +20.0))
            }
        }

        // Draw loop
        extend {
            drawer.clear(ColorRGBa.BLACK)

            // Volume demos
            // Buffers
            val volumeBuffer = volumeProcessor.volumeBuffer
            val rangeBuffer = volumeProcessor.rangeBuffer
            // Draw values
            val volumeDetectRect = rects[0]
            val volumeDynRangeRect = rects[1]
            val volumeFlashRect = rects[2]
            val loVol = -160.0
            val hiVol = 0.0
            val loThreshold = -90.0
            val hiThreshold = -40.0
            val volWidth = w / volumeProcessor.bufferSize
            val volHeight = h / (hiVol - loVol)

            drawer.drawDemo(VOLUME_DETECTION) {
                // Draw the volume buffer as a rect chart
                val offset = volumeDetectRect.corner
                for (i in volumeBuffer.indices) {
                    val vol = volumeBuffer[i]
                    val x = offset.x + i * volWidth
                    val y = offset.y + vol.map(loVol, hiVol, h, 0.0)
                    rectangle(x, y, volWidth, volHeight)
                }
            }

            drawer.drawDemo(VOLUME_DYN_RANGE) {
                // Draw the dynamic ranges as bars
                val offset = volumeDynRangeRect.corner
                for (i in rangeBuffer.indices) {
                    val range = rangeBuffer[i]
                    val lo = range.start
                    val hi = range.endInclusive
                    val x = offset.x + i * volWidth
                    val y = offset.y + hi.map(loVol, hiVol, h, 0.0)
                    val rangeH = (lo-hi).map(loVol, hiVol, h, 0.0)
                    rectangle(x, y, volWidth, rangeH)
                }
            }

            drawer.drawDemo(VOLUME_FLASH) {
                // Draw a rect that flashes when the volume is above the threshold
                // The opacity is the volume
                // The size is scaled by volume as well
                val offset = volumeFlashRect.corner
                val vol = rangeBuffer.last().endInclusive
                val volInterp = vol.map(loThreshold, hiThreshold, 0.0, 1.0).saturate()
                val opacity = volInterp
                fill = ColorXSVa((seconds*10.0)%360.0, 1.0, 1.0, opacity).toRGBa()
                stroke = null
                val scale = volInterp
                val scaledW = w * scale
                val scaledH = h * scale
                val scaledOffset = offset + Vector2((w - scaledW) / 2.0, (h - scaledH) / 2.0)
                rectangle(scaledOffset, scaledW, scaledH)
            }

            // Frequency-related demos
            // Buffers
            val pitchBuffer = pitchProcessor.pitchBuffer
            val spectrogram = pitchProcessor.spectrumBuffer
            // Draw values
            val pitchDetectionRect = rects[3]
            val spectrogramRect = rects[4]
            val percussionRect = rects[5]
            val pitchWidth = w / pitchBuffer.size
            val pitchHeight = h / PITCH_RANGE.let { it.endInclusive - it.start }

            drawer.drawDemo(PITCH_DETECTION) {
                // Draw the pitch buffer as a rect chart
                val pitchOffset = pitchDetectionRect.corner
                for (i in pitchBuffer.indices) {
                    val pitch = pitchBuffer[i]
                    val x = pitchOffset.x + i * pitchWidth
                    val y = pitchOffset.y + pitch.toDouble().map(LOWEST_PITCH.toDouble(), HIGHEST_PITCH.toDouble(), h, 0.0)
                    rectangle(x, y, pitchWidth, pitchHeight)
                }
            }

            drawer.drawDemo(SPECTROGRAM) {
                // Draw the latest spectrogram from buffer
                // Low frequencies on the left, high frequencies on the right
                val offset = spectrogramRect.corner
                val spec = spectrogram.lastOrNull() ?: return@drawDemo
                val specWidth = w / spec.size
                val specHeight = h / (pitchProcessor.largestMag * 1.2)
                for (i in spec.indices) {
                    val freq = spec[i]
                    val x = offset.x + i * specWidth
                    val y = offset.y + h - freq * specHeight
                    val sH = freq * specHeight
                    rectangle(x, y, specWidth, sH)
                }
            }

            drawer.drawDemo(PERCUSSION_FLASH) {
                // Draw the percussion salience as a flashing rect
                val offset = percussionRect.corner
                val salience = pitchProcessor.percussionSalience.saturate()
                val opacity = salience
                fill = ColorXSVa((seconds*10.0)%360.0, 1.0, 1.0, opacity).toRGBa()
                stroke = null
                val scale = salience
                val scaledW = w * scale
                val scaledH = h * scale
                val scaledOffset = offset + Vector2((w - scaledW) / 2.0, (h - scaledH) / 2.0)
                rectangle(scaledOffset, scaledW, scaledH)
            }
        }

        fun closeApp() {
            dispatcher.stop()
            application.exit()
        }

        // Keybinds
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> closeApp()
            }
        }
    }
}

enum class Demos(val trivial: String) {
    VOLUME_DETECTION("Volume detection"),
    VOLUME_DYN_RANGE("Volume dynamic range"),
    VOLUME_FLASH("Volume flash"),

    PITCH_DETECTION("Pitch detection"),
    SPECTROGRAM("Spectrogram"),
    PERCUSSION_FLASH("Percussion flash"),

}