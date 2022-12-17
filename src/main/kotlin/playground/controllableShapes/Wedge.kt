@file:Suppress("unused")

package playground.controllableShapes

import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.shape
import utils.lerp

class Wedge(var tip: Vector2, var angleFrom: Double, var angleTo: Double, var radius: Double) {

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

