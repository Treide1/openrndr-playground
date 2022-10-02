package bpm

import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorHSVa
import org.openrndr.math.map
import kotlin.math.floor

/**
 * Showcasing BPM support with a simple Attack-Decay-Sustain-Release envelope.
 * Repeats after four beats.
 * Press KEY_SPACE to reset time. Press KEY_P to pause/unpause.
 *
 * ADSR pattern: https://en.wikipedia.org/wiki/Envelope_(music)#ADSR
 */
fun main() = application {
    configure {
        width = 1280
        height = 640
        title = "BeatEnvelope Demo with Attack-Decay-Sustain-Release"
    }

    program {

        // Map of control points. Implements linear ADSR pattern.
        val envelopeControlPoints = hashMapOf(
            0.0 to .00,
            .25 to .40, // Attack
            .50 to .25, // Decay
            .75 to .25, // Sustain
            1.0 to .00  // Release
        )

        // Project specific envelope:
        // In range [0.0, 1.0] with linear interpolation between control points.
        // Can use any amount of control points, as long as they are unique in x.
        // For best results, start and end with 0.0 to continuously wrap around loop.
        val beatEnvelope = BeatEnvelope(125.0, 4) { phase ->
            lerpBetweenControlPoints(phase%2, envelopeControlPoints)
        }

        // Add clock to program. We just have one BeatEnvelope, add it here.
        val clock = extend(Clock()) {
            add(beatEnvelope)
        }

        // Amount of dots we see on the screen. Are placed equidistant across the width excluding some margin.
        val dotCount = 40

        // Easy access of beatsPerLoop in common type Double
        val beatsPerLoop = beatEnvelope.beatsPerLoop.toDouble()

        // Add draw process to program.
        extend {
            beatEnvelope.samplePhaseIndex(0.0, 4.0, dotCount) { sample, phase, index ->
                // Absolute phase of local phase added to global phase.
                val absolutePhase = phase + this.phase

                // Calculate point and point-specific values.
                val x = index.toDouble().map(0.0, dotCount.toDouble(), width*.1, width*.9)
                val y = sample.map(0.0, 1.0, 200.0, -200.0) + height/2
                val hue = floor(absolutePhase).map(0.0, beatsPerLoop, 0.0, 360.0)
                val brightness = index.toDouble().map(0.0, dotCount.toDouble(), 0.0, 1.0)

                // Draw the circle.
                drawer.fill = ColorHSVa(hue, 1.0, brightness).toRGBa()
                drawer.circle(x, y, 10.0)
            }

        }

        // Add keyboard listener.
        // On KEY_SPACEBAR, reset the phase for the beatEnvelope.
        // On "p", pauses/unpauses the beatEnvelope.
        keyboard.keyDown.listen {
            if (it.key == KEY_SPACEBAR) beatEnvelope.phase = 0.0
            if (it.name == "p") clock.toggle(beatEnvelope)
        }

    }
}

/**
 * Calculates the linear interpolation (lerp) of x for the given control points.
 * @param x Input value to be lerped.
 * @param controlPointMap hashMap of control points, where (x,y) being a point means 'map.get(x) = y'.
 * @return Lerp value for x in the "line segment" of the two corresponding control points. Should x be out of range, then 0.0 is returned.
 */
fun lerpBetweenControlPoints(x: Double, controlPointMap: HashMap<Double, Double>): Double {

    // Partition points based on x value being smaller than x (first) or greater than x (second).
    // Then find control points just before, and just after x.
    // 'Just before' means greatest lower bound of x, 'just after' means least upper bound of x.
    // Returns 0.0, if x is out of range.
    val partition = controlPointMap.keys.partition { key -> key <= x }
    if (partition.first.isEmpty() || partition.second.isEmpty()) return 0.0

    val lowerControlX = partition.first.max() // Greatest lower bound of x
    val lowerControlY = controlPointMap[lowerControlX]!!
    val upperControlX = partition.second.min() // Least upper bound of x
    val upperControlY = controlPointMap[upperControlX]!!
    return x.map(lowerControlX, upperControlX, lowerControlY, upperControlY)
}
