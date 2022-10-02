package playground

import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.timeoperators.LFO
import org.openrndr.extra.timeoperators.TimeOperators
import org.openrndr.math.map
import org.openrndr.math.mod

/**
 * Based on a BPM beat, a waterfall of dots is moving from the right to left
 */
fun main() = application {

    configure {
        width = 1280
        height = 640
        title = "LFO WaveScan EXAMPLE"
    }

    program {
        val bpm = 125
        val beatLFO = LFO()

        extend(TimeOperators()) {
            track(beatLFO)
        }

        val sampleFromX = width*.9
        val sampleToX = width*.1
        val sampleFromY = height*.1
        val sampleToY = height*.9

        val sampleVals = mutableListOf<Double>()
        val sampleLength = 40

        var phase = 0.0

        extend {
            sampleVals.add(0, beatLFO.sample(frequency = bpm/60.0, phase = phase))
            if (sampleVals.size > sampleLength) sampleVals.removeLast()

            val xInc = (sampleToX - sampleFromX) / sampleLength
            sampleVals.forEachIndexed { index, sample ->
                val x = sampleFromX + index * xInc
                val y = sample.map(0.0, 1.0, sampleFromY, sampleToY)
                drawer.fill = ColorRGBa.WHITE.shade(1 - index.toDouble()/sampleLength)
                drawer.circle(x,y, 10.0)
            }

        }

        keyboard.keyDown.listen {

            when (it.key) {
                KEY_SPACEBAR -> {
                    sampleVals.firstOrNull()?.let { lastPhase -> phase = mod(phase - lastPhase + 1.0, 1.0) }
                    sampleVals.clear()
                }
            }
        }
    }
}
