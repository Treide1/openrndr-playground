package playground.controllableShapes

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.shape

class Wedge(var tip: Vector2, var angleFrom: Double, var angleTo: Double, var radius: Double) {

    var strokeColor = ColorRGBa.WHITE
    var fillColor = ColorRGBa.BLUE

    val shape: Shape
        get() = createShape()

    private val scaledUnitX: Vector2
        get() = Vector2.UNIT_X*radius

    fun createShape() = shape {
        val outerFrom = tip + scaledUnitX.rotate(angleFrom)
        val outerMiddle = tip + scaledUnitX.rotate(angleFrom.lerp(angleTo, .5))
        val outerTo = tip + scaledUnitX.rotate(angleTo)
        contour {
            moveTo(tip)
            lineTo(outerFrom)
            circularArcTo(outerMiddle, outerTo)
            lineTo(tip)
            close()
        }
    }
}

fun Drawer.wedge(wedge: Wedge) {
    this.isolated {
        stroke = wedge.strokeColor
        fill = wedge.fillColor
        shape(wedge.shape)
    }
}

/**
 * @receiver lerpSource
 * @param B lerpTarget
 * @param perc percentage to lerp from A to B
 */
private fun Double.lerp(B: Double, perc: Double): Double {
    return this*(1-perc) + B*perc
}
