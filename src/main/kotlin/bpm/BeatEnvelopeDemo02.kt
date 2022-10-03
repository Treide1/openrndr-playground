package bpm

import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.map

/**
 * Showcasing BPM support with the BeatEnvelope class.
 * We sample two Oscillator envelopes and blend them together.
 */
fun main() = application {
    configure {
        width = 1280
        height = 640
        title = "BeatModulator Demo"
    }

    program {

        // Config
        val bpm = 121.0 // Song: "Step-Grandma" by Salvatore Ganacci
        val sampleCount = 40

        val lowerY = height*.9
        val upperY = height*.1
        val leftX = width*.2
        val rightX = width*.8
        val rad = 10.0
        val leftColor = ColorRGBa.GREEN
        val rightColor = ColorRGBa.RED

        // Creating BeatEnvelopes and adding them to clock
        val lowFreqSine = constructSine(bpm, 4, 4.0)
        val highFreqSine = constructSine(bpm, 4, 1.0, .4)

        val clock = extend(Clock()) {
            add(lowFreqSine)
            add(highFreqSine)
        }

        // Calculating weights in advance
        val decreasingWeights = interpolationList(0.0, 1.0, sampleCount).map { 1.0 - it }
        val increasingWeights = interpolationList(0.0, 1.0, sampleCount).map { it }

        extend {

            // Weighted sum of envelope samples
            val pastLFO = lowFreqSine.sampleList(0.0, 1.0, sampleCount)
            val pastHFO = highFreqSine.sampleList(0.0, 1.0, sampleCount)

            val weightedLFO = pastLFO mult decreasingWeights
            val weightedHFO = pastHFO mult increasingWeights

            val mixedSamples = weightedLFO add weightedHFO

            // Comparison circle left
            drawer.isolated {
                fill = leftColor
                val x = (0 + leftX) * .5
                val y = lowFreqSine.sample().map(0.0, 1.0, lowerY, upperY)
                circle(x, y, rad)
            }
            // Comparison circle right
            drawer.isolated {
                fill = rightColor
                val x = (width + rightX) * .5
                val y = highFreqSine.sample().map(0.0, 1.0, lowerY, upperY)
                circle(x, y, rad)
            }

            // Interpolation circles
            drawer.isolated {
                mixedSamples.forEachIndexed { index, sample ->
                    val frac = index.toDouble().map(0.0, mixedSamples.lastIndex.toDouble(), 0.0, 1.0)
                    val x = frac.map(0.0, 1.0, leftX, rightX)
                    val y = sample.map(0.0, 1.0, lowerY, upperY)
                    val color = leftColor.mix(rightColor, frac)

                    fill = null
                    stroke = color
                    circle(x, y, rad)
                }
            }
        }

        // Add keyboard listener.
        // On KEY_SPACEBAR, reset the phase for the beatEnvelopes.
        // On "p", pauses/unpauses the beatEnvelopes.
        val sines = listOf(lowFreqSine, highFreqSine)
        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) sines.forEach { env -> env.phase = 0.0 }
            if (it.name == "p") sines.forEach { env -> clock.toggle(env) }
        }
    }
}
