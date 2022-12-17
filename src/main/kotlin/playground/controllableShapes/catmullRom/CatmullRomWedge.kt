package playground.controllableShapes.catmullRom

import org.openrndr.math.Vector2
import org.openrndr.shape.ContourBuilder
import org.openrndr.shape.Shape
import org.openrndr.shape.shape
import playground.controllableShapes.catmullRom.CatmullRomBuilder.Companion.EndpointStrategy
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

    private fun interpolationList(start: Vector2, end: Vector2): List<Vector2> {
            val diff = end - start
            val normal = diff.rotate(-90.0)
            relPointList.add(0, Vector2(0.0, 0.0))
            relPointList.add(Vector2(1.0, 0.0))
            return relPointList.map { p ->
                start + diff * p.x + normal * p.y
            }
        }

    private fun createShape() = shape {
        contour {
            moveTo(tip)

            catmullRomSpline(WITH_TANGENT, alpha) {
                interpolationList(tip, outerFrom).forEach { add(it) }
            }

            circularArcTo(outerMiddle, outerTo)
            lineTo(tip)
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

    private fun ContourBuilder.catmullRomSpline(
        endpointStrategy: EndpointStrategy, alpha: Double, function: CatmullRomBuilder.() -> Unit)
    {
        val cmb = CatmullRomBuilder(cursor, endpointStrategy, alpha)
        cmb.function()
        val segList = cmb.buildBezierSegments()
        segList.forEach { seg ->
            curveTo(seg.p1, seg.p2, seg.p3)
        }
    }

}
