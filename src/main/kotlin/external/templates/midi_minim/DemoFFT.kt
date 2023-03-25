package external.templates.midi_minim

import ddf.minim.Minim
import ddf.minim.analysis.FFT
import ddf.minim.analysis.LanczosWindow

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.minim.minim
import utils.lerp
import kotlin.math.ln

fun main() {
    application {
        configure {
            width = 1280
            height = 720
        }

        program {
            val minim = minim()
            val lineIn = minim.getLineIn(Minim.MONO, 2048, 48000f)
            val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
            fft.window(LanczosWindow())

            val n = fft.specSize().div(4)

            val w = width * 0.8
            val lowY = 0.9 * height
            val highY = 0.1 * height
            val firstX = (width-w)/2

            extend {
                fft.forward(lineIn.mix)

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
