package playground.minim.copilot

import org.openrndr.application

fun main() = application {
    configure {
        // Fullscreen config
    }
    program {

        // Setup minim using the openrndr minim extra

        // Define bass, mid and treble frequency ranges

        // Define a conversion procedure from a freq range to an intensity value

        // Define an intensityBuffer for each freq range

        // Define a center rectangle drawRect, using 80% of the viewport and having y flipped.

        extend {
            // For each freq range, perform the intensity calculation.
            // Then update the intensityBuffer with the new value if it is bigger.
            // Otherwise, multiply the current value by a damp factor of 0.95 .

            // Within the drawRect, use the intensity buffer to draw 3 equidistant circles.
            // The radius of each circle is proportional to the intensity from the intensityBuffer.
        }
    }
}