package playground.iterativeImageWarping

import org.openrndr.math.Vector2

/**
 * Provider interface for provision of a warp function V : (Vector2) -> Vector2.
 * This is the offset from a point p to p + V(p).
 */
interface WarpProvider {

    /**
     * Given a warp origin [x_w], current time [t] and last time step [delta_t],
     * return a warp offset function.
     */
    fun getV(x_w: Vector2, t: Double, delta_t: Double): (p: Vector2) -> Vector2
}