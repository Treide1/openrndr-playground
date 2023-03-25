package external.templates.midi_minim

import ddf.minim.Minim
import ddf.minim.analysis.FFT
import ddf.minim.analysis.LanczosWindow

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.minim.minim
import org.openrndr.math.IntVector2
import utils.lerp
import kotlin.math.ln

fun main() {
    application {
        configure {
            width = 640
            height = 480
            position = IntVector2.ZERO
        }

        program {
            // Setup minim
            val minim = minim()
            // Uses system standard audio input device.
            // This depends on system settings (usually your mic).
            // To use application audio output, you have to route via your system.
            // For Windows, see "Stereo Mix". For macOS, see "Blackhole".
            val lineIn = minim.getLineIn(Minim.MONO, 2048, 48000f)
            // FFT setup with config
            val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
            fft.window(LanczosWindow())

            // Drawing (for illustration purposes)
            val n = fft.specSize().div(4)

            val w = width * 0.8
            val lowY = 0.9 * height
            val highY = 0.1 * height
            val firstX = (width-w)/2

            extend {
                // Feed the fft with the latest audio data
                fft.forward(lineIn.mix)

                // Draw the fft result
                for (i in 0 until n) {
                    val freq = fft.getBand(i)
                    val freqCount = fft.timeSize() // same as bufferSize
                    val bandDB = ln(2.0 * freq / freqCount) // frequency to decibel
                    val relVolume = bandDB / -15.0 // should be much closer to [0, 1]

                    val x = firstX + i * w / n
                    val targetY = lowY.lerp(highY, relVolume)

                    drawer.stroke = ColorRGBa.WHITE
                    drawer.lineSegment(x, lowY, x, targetY)
                }
            }
        }
    }
}
