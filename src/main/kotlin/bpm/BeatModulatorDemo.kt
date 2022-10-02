package bpm

import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.map

/**
 * Showcasing BPM support with the BeatModulator class.
 * We sample two Oscillator envelopes and blend them together.
 */
fun main() = application {
    configure {
        width = 1280
        height = 640
        title = "BeatModulator Demo"
    }

    program {

        val bpm = 128.0
        val beatModulator = BeatModulator()

        val clock = extend(Clock()) {
            add(beatModulator)
        }

        val lowFreqSine = constructSine(bpm, 4, 4.0)
        val highFreqSine = constructSine(bpm, 4, 1.0, .4)

        val lfoID = 0
        val hfoID = 1
        beatModulator[lfoID] = lowFreqSine
        beatModulator[hfoID] = highFreqSine

        val sampleCount = 40

        val decreasingWeights = interpolationList(0.0, 1.0, sampleCount).map { 1.0 - it }
        val increasingWeights = interpolationList(0.0, 1.0, sampleCount).map { it }

        extend {

            val lowerY = height*.9
            val upperY = height*.1
            val leftX = width*.2
            val rightX = width*.8
            val rad = 10.0
            val leftColor = ColorRGBa.GREEN
            val rightColor = ColorRGBa.RED

            val pastLFO = beatModulator.sampleList(lfoID, 0.0, 1.0, sampleCount)
            val pastHFO = beatModulator.sampleList(hfoID, 0.0, 1.0, sampleCount)

            val weightedLFO = pastLFO mult decreasingWeights
            val weightedHFO = pastHFO mult increasingWeights

            val mixedSamples = weightedLFO add weightedHFO

            drawer.apply {
                fill = leftColor
                val x = (0 + leftX) * .5
                val y = lowFreqSine.sample().map(0.0, 1.0, lowerY, upperY)
                circle(x, y, rad)
            }
            drawer.apply {
                fill = rightColor
                val x = (width + rightX) * .5
                val y = highFreqSine.sample().map(0.0, 1.0, lowerY, upperY)
                circle(x, y, rad)
            }

            drawer.circles {
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
        // On KEY_SPACEBAR, reset the phase for the beatEnvelope.
        // On "p", pauses/unpauses the beatEnvelope.
        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) beatModulator.syncAll()
            if (it.name == "p") clock.toggle(beatModulator)
        }
    }
}
