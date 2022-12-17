package playground.controllableShapes

import org.openrndr.math.Vector2
import org.openrndr.shape.ContourBuilder
import org.openrndr.shape.Shape
import org.openrndr.shape.shape
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
                interpolationList(cursor, outerFrom).forEach { add(it) }
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

    companion object {
        // Refactor task: Move to enum
        const val WITH_TANGENT = 0
        const val WITH_MIRROR = 1
    }


    private fun ContourBuilder.catmullRomSpline(
        endPointStrategy: Int, alpha: Double, function: CatmullRomBuilder.() -> Unit)
    {
        val cmb = CatmullRomBuilder(cursor, endPointStrategy, alpha)
        cmb.function()
        val segList = cmb.buildBezierSegments()

        segList.forEach { seg ->
            curveTo(seg.p1, seg.p2, seg.p3)
        }
    }

    class CatmullRomBuilder(val start: Vector2, val endPointStrategy: Int = WITH_TANGENT, val alpha: Double = .5) {

        private val pointList = mutableListOf<Vector2>()

        fun add(p: Vector2) {
            pointList.add(p)
        }

        fun add(x: Double, y: Double) {
            add(Vector2(x, y))
        }

        /**
         * Computation of bezier segments. Adds endpoints according to strategy.
         *
         * Converts Catmull Rom spline to Hermite spline.
         * Then Hermite spline to Bezier spline.
         * Returned as list of [BezierSegment] (point quadruples)
         */
        fun buildBezierSegments(): List<BezierSegment> {

            // Guard: Add least 2 points are needed.
            if (pointList.size <= 1) return listOf()

            // Add endpoints according to strategy.
            when (endPointStrategy) {
                WITH_TANGENT -> {
                    // Add virtual endpoints in the direction of the total tangent.
                    // Dist to specified endpoints is equal to tangent-component of first and last linear segment.
                    val n = pointList.size
                    val vTotal = (pointList[n-1] - pointList[0]).normalized

                    val vFirst = (pointList[1] - pointList[0]) // First linear segment
                    val vLast = (pointList[n-1] - pointList[n-2]) // Last linear segment
                    val lenFirst = vTotal.dot(vFirst)
                    val lenLast = vTotal.dot(vLast)

                    val v0 = pointList.first() - vTotal.times(lenFirst)
                    val v1 = pointList.last() + vTotal.times(lenLast)

                    pointList.add(0, v0)
                    pointList.add(v1)
                }
                WITH_MIRROR -> {
                    // Add virtual endpoints as mirror of first and last linear segment
                    val n = pointList.size

                    val vFirst = (pointList[1] - pointList[0]) // First linear segment
                    val vLast = (pointList[n-1] - pointList[n-2]) // Last linear segment

                    pointList.add(0, pointList.first() - vFirst)
                    pointList.add(pointList.last() - vLast)
                }
                else -> {}
            }

            // Guard: Add least 4 post-addition points are needed.
            if (pointList.size <= 3) return listOf()

            val result = mutableListOf<BezierSegment>()
            val n = pointList.size
            for(i in pointList.indices) {
                // Guard: Iteration over point quadruples. i is always second entry of quadruple.
                // Thus, i can not be first, or last (=n-1), or second to last (=n-2).
                if (i == 0 || i >= n-2) continue

                // Catmull-Rom spline converted down to BezierSegment
                val seg = CatmullRomSegment(pointList[i-1], pointList[i], pointList[i+1], pointList[i+2], alpha)
                    .toHermiteSegment()
                    .toBezierSegment()

                result.add(seg)
            }

            return result
        }
    }

}

data class CatmullRomSegment(val p0: Vector2, val p1: Vector2, val p2: Vector2, val p3: Vector2, val alpha: Double)
data class HermiteSegment(val p0: Vector2, val v0: Vector2, val p1: Vector2, val v1: Vector2)
data class BezierSegment(val p0: Vector2, val p1: Vector2, val p2: Vector2, val p3: Vector2)

fun CatmullRomSegment.toHermiteSegment(): HermiteSegment {
    val p0 = this.p1
    val p1 = this.p2
    val v0 = (this.p2-this.p0).times(alpha)
    val v1 = (this.p2-this.p0).times(alpha)

    return HermiteSegment(p0, v0, p1, v1)
}

fun HermiteSegment.toBezierSegment(): BezierSegment {
    val c0 =  this.p0 + this.v0.times(1/3.0)
    val c1 = this.p1 - this.v1.times(1/3.0)

    return BezierSegment(p0, c0, c1, p1)
}
