package playground.controllableShapes.catmullRom

import org.openrndr.math.Vector2

data class CatmullRomSegment(val p0: Vector2, val p1: Vector2, val p2: Vector2, val p3: Vector2, val alpha: Double)
data class HermiteSegment(val p0: Vector2, val v0: Vector2, val p1: Vector2, val v1: Vector2)
data class BezierSegment(val p0: Vector2, val p1: Vector2, val p2: Vector2, val p3: Vector2)

fun CatmullRomSegment.toHermiteSegment(): HermiteSegment {
    val p0 = this.p1
    val p1 = this.p2
    val v0 = (this.p2-this.p0).times(alpha)
    val v1 = (this.p3-this.p1).times(alpha)

    return HermiteSegment(p0, v0, p1, v1)
}

fun HermiteSegment.toBezierSegment(): BezierSegment {
    val c0 =  this.p0 + this.v0.times(1/3.0)
    val c1 = this.p1 - this.v1.times(1/3.0)

    return BezierSegment(p0, c0, c1, p1)
}