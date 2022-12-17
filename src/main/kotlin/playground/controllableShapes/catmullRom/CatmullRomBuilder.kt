package playground.controllableShapes.catmullRom

import org.openrndr.math.Vector2
import playground.controllableShapes.catmullRom.CatmullRomBuilder.Companion.EndpointStrategy.*

class CatmullRomBuilder(val start: Vector2, var endpointStrategy: EndpointStrategy, var alpha: Double) {

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
        when (endpointStrategy) {
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
        }

        // Guard: Add least 4 post-addition points are needed.
        // Not needed if EndpointStrategy adds virtual endpoints on each end.
        if (pointList.size <= 3) return listOf()

        val result = mutableListOf<BezierSegment>()
        val n = pointList.size
        for(i in pointList.indices) {
            // Guard: Iteration over point quadruples. i is always second entry of quadruple.
            // Thus, i can not be first, or last (=n-1), or second to last (=n-2).
            if (i == 0 || i >= n-2) continue

            // Catmull-Rom spline converted down to BezierSegment
            val seg = CatmullRomSegment(pointList[i - 1], pointList[i], pointList[i + 1], pointList[i + 2], alpha)
                .toHermiteSegment()
                .toBezierSegment()

            result.add(seg)
        }

        return result
    }

    companion object {
        /**
         * Defines strategies on how to add (virtual) end points for Catmull-Rom spline.
         * The virtual points are not interpolated through, but determine the direction
         * of leaving the first point/entering the last point.
         */
        enum class EndpointStrategy{
            /**
             * Add a virtual point in the direction of the tangent through the first and last point on each end.
             */
            WITH_TANGENT,

            /**
             * Add a virtual point in the direction of the first/last linear segment.
             * The second point is mirrored on the first/the second-last point is mirrored on the last.
             */
            WITH_MIRROR
        }
    }
}