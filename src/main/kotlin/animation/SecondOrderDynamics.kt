package animation

import kotlin.math.PI
import kotlin.math.pow

/**
 * Target track of 1-d variable x using second order dynamics.
 * Source: https://www.youtube.com/watch?v=KPoeNZZ6H4s by t3ssel8r
 *
 * Initialize a target with x0, then [update] with the passed time T and the new target x.
 * The system will track the target value based on a second order differential system of equations.
 *
 * The resulting dynamics are useful for animation, the tracking behaviour
 * can be controlled via (mostly independent) control parameters [f], [z] and [r].
 */
class SecondOrderDynamics(f: Double, z: Double, r:Double, x0: Double) {

    /**
     * Frequency f of response oscillation.
     *
     * Measured in units/second.
     * Does not control the shape of the response.
     */
    var f = f
        set(value) {
            field = value
            calculateKs()
        }

    /**
     * Damping coefficient zeta of response.
     *
     * At 0.0, no dampening happens. Response doesn't die down.
     * In open interval (0.0, 1.0) dampens but overshoots target.
     * For 1.0 or more, the damp closes in on target without overshoot.
     */
    var z = z
        set(value) {
            field = value
            calculateKs()
        }

    /**
     * Reaction vale r of response.
     *
     * At r = 0.0, the response accelerates from rest (0 = y' = y'' = ...).
     * At r > 0.0, the reaction sharply starts.
     * At r < 0.0, the reaction anticipates before moving to target.
     */
    var r = r
        set(value) {
            field = value
            calculateKs()
        }

    private var k1 = 0.0
    private var k2 = 0.0
    private var k3 = 0.0

    private var xp = x0

    /**
     * Current y value.
     */
    var y = x0
        private set

    /**
     * Previous y value.
     */
    var yp = 0.0
        private set
    private var yd = 0.0

    init {
        calculateKs()
    }

    fun calculateKs() {
        if (f == 0.0) println("Division by f=$f, not caught at calc site.")

        k1 = z / (PI * f)
        k2 = 1 / (2 * PI * f).pow(2)
        k3 = r * z / (2 * PI * f)
    }

    /**
     * Update the system with a new target [x] and the passed time [T] since the last update.
     * Also provide the real derivative [xd] of the tracked target value.
     */
    fun update(T: Double, x: Double, xd: Double) : Double {
        xp = x // Update previous x
        yp = y // Update previous y
        y += T * yd // Calc y
        yd += T * (x + k3 * xd - y - k1 * yd) / k2 // Calc y'
        return y
    }

    /**
     * Update the system with a new target [x] and the passed time [T] since the last update.
     * Estimates the derivative of the target value to be linear since last update.
     */
    fun update(T: Double, x: Double) : Double {
        val xd = (x - xp) / T // Estimate x'
        xp = x // Update previous x
        yp = y // Update previous y
        y += T * yd // Calc y
        yd += T * (x + k3 * xd - y - k1 * yd) / k2 // Calc y'
        return y
    }

}