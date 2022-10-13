package bpm

import org.openrndr.KEY_SPACEBAR
import org.openrndr.animatable.easing.Easing
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.map

/**
 * Showcasing BPM support with the BeatModulator class.
 * We sample two Oscillator envelopes and modulate them together.
 * Transitions on hitting SPACEBAR.
 *
 * Author: Lukas Henke, 03.10.2022
 *
 * Controls:
 * SPACEBAR - Change target modulation (via transition)
 * "c" - Cancel all transitions. Modulation doesn't change anymore.
 * "s" - Sync the BeatEnvelopes to the moment of key press
 * "p" - Pause the enitre program
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
        val leftX = width*.3
        val rightX = width*.9
        val rad = 10.0
        val leftColor = ColorRGBa.GREEN
        val rightColor = ColorRGBa.RED

        val transitionEasing = Easing.CubicOut

        // Creating BeatModulator and adding it to clock
        val beatModulator = BeatModulator()

        val clock = extend(Clock()) {
            add(beatModulator)
        }

        // Adding two beatEnvelopes to the beatModulator
        val lfs = constructSine(bpm, 4, 4.0) // lowFreqSine
        val hfs = constructSine(bpm, 4, 1.0, .4) // highFreqSine
        beatModulator.envelopes[0] = lfs
        beatModulator.envelopes[1] = hfs

        // We also set the initial weights
        val targetWeights = mutableMapOf(0 to 1.0, 1 to 0.0)
        beatModulator.setWeightsAfterTransitions(targetWeights)

        extend {

            // Comparison circle left
            drawer.isolated {
                fill = leftColor
                val x = leftX*.3
                val y = lfs.sample().map(0.0, 1.0, lowerY, upperY)
                circle(x, y, rad)
            }
            // Comparison circle right
            drawer.isolated {
                fill = rightColor
                val x = leftX*.7
                val y = hfs.sample().map(0.0, 1.0, lowerY, upperY)
                circle(x, y, rad)
            }

            drawer.isolated {
                val weightedSamples = beatModulator.sampleList(0.0, 1.0, sampleCount)
                weightedSamples.forEachIndexed { index, sample ->
                    val frac = index.toDouble().map(0.0, weightedSamples.lastIndex.toDouble(), 0.0, 1.0)
                    val x = frac.map(0.0, 1.0, leftX, rightX)
                    val y = sample.map(0.0, 1.0, lowerY, upperY)
                    val color = leftColor.mix(rightColor, beatModulator.weights[1].value)

                    fill = null
                    stroke = color
                    circle(x, y, rad)
                }
            }
        }

        // Add keyboard listener.
        // On KEY_SPACEBAR, swaps the current targetWeights and pushes them to transitions over 2 seconds.
        // On "s", syncs the phase for the beatEnvelopes.
        // On "p", pauses/unpauses the beatEnvelopes.
        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) {
                val tmp0 = targetWeights[0]!!
                val tmp1 = targetWeights[1]!!
                targetWeights[0] = tmp1
                targetWeights[1] = tmp0

                // After all transitions, do one more swap
                beatModulator.pushTransition(targetWeights, 2.0, transitionEasing)
            }
            if (it.name == "c") beatModulator.cancelAllTransitions()
            if (it.name == "s") beatModulator.syncAll(null, 0.0)
            if (it.name == "p") clock.toggle(beatModulator)
        }
    }
}
