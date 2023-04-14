package playground.minim.copilot

import org.openrndr.application

// Uncomment to see the result. Doesn't compile as-is, so it's commented out.

/*
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

        // Define a list of frequency ranges with named access via "bass", "mid" and "treble"
        val freqRanges = listOf(
            "bass" to 20.0..160.0,
            "mid" to 160.0..320.0,
            "treble" to 320.0..20000.0
        )

        // Define a conversion function that samples the fft for all frequencies in the range.
        // Then, it returns the average of the squared values.
        fun freqRangeToIntensity(range: ClosedFloatingPointRange<Double>): Double {
            val start = range.start.toInt()
            val end = range.endInclusive.toInt()
            return (start..end).map { fft.spectrum[it].toDouble().pow(2) }.average()
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
                val intensity = freqRangeToIntensity(range)
                intensityBuffer[i] = intensityBuffer[i].coerceAtLeast(intensity) * 0.95
            }

            // Within the drawRect, use the intensity buffer to draw 3 equidistant circles.
            // The radius of each circle is proportional to the intensity from the intensityBuffer.
            drawer.fill = null
            drawer.stroke = null
            intensityBuffer.forEachIndexed { i, intensity ->
                val radius = intensity * drawRect.height / 2
                drawer.circle(drawRect.center + (i - 1) * drawRect.width / 4, radius)
            }
        }
    }
}
*/