package playground.iterativeImageWarping

import org.openrndr.math.*
import org.openrndr.math.transforms.transform
import playground.h
import playground.w
import utils.TAU
import kotlin.math.pow
import kotlin.math.sin

val center = Vector2(w /2.0, h /2.0)

/**
 * Provides a warp function that performs Scale-Rotate with origin of viewport center.
 */
object CentricScaleRotateWarper : WarpProvider {
    // Warp func
    var ang = 0.0
    val angOff = 5.0
    var scl = 1.0
    val sclOff = 0.1

    override fun getV(x_w: Vector2, t: Double, delta_t: Double) : (Vector2) -> Vector2 {
        var M = Matrix44.IDENTITY

        // rotate around center by angle
        M = M.transform {
            translate(center)
            scale(scl)
            rotate(ang)
            translate(-center)
            //translate(100.0, 100.0)
        }
        return { p: Vector2 ->
            (M * p.xy01).xyEuc - p
        }
    }
}

/**
 * Provide a shear warp, that maps ax + by (where a,b are coefficients) to an angle
 * and the angle is mapped to an offset from polar coords.
 */
object ShearWarper : WarpProvider {
    override fun getV(x_w: Vector2, t: Double, delta_t: Double): (Vector2) -> Vector2 {
        val ang = (x_w * Vector2(5.0/w, 10.0/h))
            .run { x + y }
            .let { sin(it * TAU) }
            .map(0.0, 1.0, -15.0, 15.0)
        return { _ ->
            Polar(ang, 30.0).cartesian
        }
    }
}

/**
 * Wobble over time
 */
object WobbleWarper : WarpProvider {
    override fun getV(x_w: Vector2, t: Double, delta_t: Double): (p: Vector2) -> Vector2 {


        return { p ->
            val ang = (p * Vector2(2.0/w, 2.0/h))
                .run { sin(x * TAU) * sin(y * TAU)}
                .map(0.0, 1.0, -15.0, 15.0)
            Polar(ang, 30.0).cartesian
        }
    }

}

private val Vector4.xyEuc: Vector2
    get() = Vector2(x/w, y/w)