package playground.controllableShapes.catmullRom

import org.openrndr.math.Vector2
import org.openrndr.shape.Shape
import org.openrndr.shape.shape
import playground.controllableShapes.catmullRom.CatmullRomBuilder.Companion.EndpointStrategy.*
import utils.lerp

class CatmullRomWedge(var tip: Vector2, var angleFrom: Double, var angleTo: Double, var radius: Double) {

    val shape: Shape
        get() = createShape()

    private val scaledUnitX: Vector2
        get() = Vector2.UNIT_X*radius

    private val outerFrom
        get() = tip + scaledUnitX.rotate(angleFrom)
    private val outerMiddle
        get() = tip + scaledUnitX.rotate(angleFrom.lerp(angleTo, .5))
    private val outerTo
        get() = tip + scaledUnitX.rotate(angleTo)

    var alpha = .5

    private val relPointList = mutableListOf<Vector2>()

    private fun interpolationList(start: Vector2, end: Vector2, flippedRelPoints: Boolean = false): List<Vector2> {
        val diff = end - start
        val normal = diff.rotate(-90.0)

        var list = relPointList.sandwichAdd(Vector2.ZERO, Vector2.UNIT_X)

        list = list.map { p ->
            if (flippedRelPoints) Vector2(1.0-p.x, p.y) else p
        }
        if (flippedRelPoints) list = list.reversed()

        return list.map { (x, y) ->
            start + diff * x + normal * y
        }
    }

    private fun createShape() = shape {
        contour {
            moveTo(tip)

            catmullRomSpline(WITH_TANGENT, alpha) {
                interpolationList(tip, outerFrom).forEach { add(it) }
            }

            circularArcTo(outerMiddle, outerTo)

            catmullRomSpline(WITH_TANGENT, alpha) {
                interpolationList(outerTo, tip, flippedRelPoints = true).forEach { add(it) }
            }

            close()
        }
    }

    fun addRelativePoint(p: Vector2) {
        relPointList.add(p)
    }

    fun addRelativePoint(x: Double, y: Double) {
        addRelativePoint(Vector2(x, y))
    }

    fun clearRelPoints() { relPointList.clear() }

}

/**
 * Add an element to each end of [this] list, one to the front and one to the back.
 * Thus, you make a element-list-element sandwich, returned as list.
 */
private fun <T> List<T>.sandwichAdd(front: T, back: T) : List<T> {
    return listOf(front) + this + back
}
