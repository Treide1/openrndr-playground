package playground

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import org.openrndr.math.map
import utils.hullSpike

/**
 * Application to show parabolic movement.
 * Has 3 points: start, end and control
 * Fades in from start, fades out on end.
 * Clicking with mouse resets control point. Then animation restarts.
 */
fun main() = application {
    configure {
        width = 640
        height = 640
        title = "Movement Example"
    }

    program {
        val b = drawer.bounds
        val off = Vector2(120.0, 120.0)
        var arc = ParabolicArc(
            b.corner + off,
            b.dimensions.copy(y=0.0) + off.rotate(90.0),
            b.center
        )
        var currentFrame = 0

        extend {
            currentFrame++

            val t = currentFrame.toDouble().map( 0.0, 240.0, 0.0, 1.0)
            drawer.fill = ColorRGBa.WHITE.opacify(hullSpike(t))
            drawer.circle(arc.eval(t), 10.0)

            drawer.fill = ColorRGBa.GREEN
            listOf(arc.start, arc.control, arc.end).forEach {
                drawer.circle(it, 20.0)
            }
        }

        mouse.buttonDown.listen {
            arc = arc.copy(peak = mouse.position)
            currentFrame = 0
        }
    }
}

/////////////////////

/**
 * ParabolicArc:
 * Lagrange Interpolation Formula
 * P(x) = y1 * (x - x2)(x - x3)/(x1 - x2)(x1 - x3) + ...
 */
class ParabolicArc(val start: Vector2, val end: Vector2, val control: Vector2){

    private fun diffProd(x: Double, vararg others: Double): Double {
        var prod = 1.0
        others.forEach { prod *= (x - it) }
        return prod
    }
    val x1 = start.x
    val x2 = end.x
    val x3 = control.x
    val y1 = start.y
    val y2 = end.y
    val y3 = control.y
    val d1 = diffProd(x1, x2, x3)
    val d2 = diffProd(x2, x1, x3)
    val d3 = diffProd(x3, x1, x2)

    fun eval(t: Double): Vector2 {
        val x = t.map(0.0, 1.0, start.x, end.x)
        val y = y1*diffProd(x, x2, x3)/d1 + y2*diffProd(x, x1, x3)/d2 + y3*diffProd(x, x1, x2)/d3
        return Vector2(x,y)
    }

    fun copy(start: Vector2 = this.start, end: Vector2 = this.end, peak: Vector2 = this.control): ParabolicArc {
        return ParabolicArc(start, end, peak)
    }
}

