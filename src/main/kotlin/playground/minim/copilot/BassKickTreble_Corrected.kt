package playground.minim.copilot

// Added by me: IntelliJ IDEA import completion.
import ddf.minim.Minim
import ddf.minim.analysis.FFT
import ddf.minim.analysis.LanczosWindow
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa.Companion.BLACK
import org.openrndr.color.ColorRGBa.Companion.PINK
import org.openrndr.color.ColorRGBa.Companion.WHITE
import org.openrndr.extra.minim.minim
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import utils.smoothstep
import kotlin.math.absoluteValue
import kotlin.math.pow

fun main() = application {
    configure {
        // Fullscreen config
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {

        // Setup minim using the openrndr minim extra
        val minim = minim()
        val lineIn = minim.getLineIn(Minim.MONO, 2048 / 2, 48000f)
        val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())
        fft.window(LanczosWindow()) // Added by me: use a lanczos window

        // Define a list of frequency ranges with named access via "bass", "mid" and "treble"
        val freqRanges = listOf(
            "bass" to 20.0..160.0,
            "mid" to 160.0..320.0,
            "treble" to 320.0..20000.0
        )

        // Define a conversion function that samples the fft for all frequencies in the range.
        // Then, it returns the average of the squared values.
        fun freqRangeToIntensity(range: ClosedFloatingPointRange<Double>, index: Int): Double {
            // Added by me: This was rewritten to apply my system- and audio-specific correction.
            // It uses a fitting curve to approximate the perceived loudness.
            // Copilot got stuck on a solution, that looks a lot like traditional Minim for Processing.
            val start = range.start.toFloat()
            val end = range.endInclusive.toFloat()
            val avg = fft.calcAvg(start, end)

            val scl = 1.0
            val off = 0.0
            val correctedAvg = ((avg * 0.05 * 3.0.pow(index) + off) * scl).smoothstep(0.0, 1.5)

            return correctedAvg
        }

        // Define an intensityBuffer to remember the calculated frequencies
        val intensityBuffer = MutableList(freqRanges.size) { 0.0 }

        // Define a center rectangle drawRect, using 80% of the viewport and having y flipped
        val drawRect = Rectangle(width * 0.1, height * 0.9, width * 0.8, -height * 0.8)

        extend {
            // Feed the fft with the lineIn data
            fft.forward(lineIn.mix)

            // For each freq range, perform the intensity calculation.
            // Then update the intensityBuffer with the new value if it is bigger.
            // Otherwise, multiply the current value by a damp factor of 0.95
            freqRanges.forEachIndexed { i, (name, range) ->
                val intensity = freqRangeToIntensity(range, i) // added by me: pass index for correction
                intensityBuffer[i] = intensityBuffer[i].coerceAtLeast(intensity) * 0.95
            }

            // Within the drawRect, use the intensity buffer to draw 3 equidistant circles.
            // The radius of each circle is proportional to the intensity from the intensityBuffer.
            // Also display the name of the range underneath the circle
            intensityBuffer.forEachIndexed { i, intensity ->
                drawer.fill = WHITE // fixed by me: use color, and use inside the loop
                drawer.stroke = BLACK // fixed by me: use color, and use inside the loop
                val radius = intensity * drawRect.height.absoluteValue / 2 // fixed by me: use absoluteValue for height
                val correctedRadius = radius.coerceIn(10.0, drawRect.height.absoluteValue / 2) // added by me: coerce radius
                val pos = drawRect.center + Vector2((i - 1) * drawRect.width / 4, 0.0) // added by me: wrap in Vector2(x, 0.0)
                drawer.circle(pos, correctedRadius) // fixed by me: use pos and correctedRadius instead
                drawer.fill = PINK // added by me: new color for text
                drawer.text(freqRanges[i].first, pos)
            }
        }

        // Added by me: This block.
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
            }
        }
    }
}