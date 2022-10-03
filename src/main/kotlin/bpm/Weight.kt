package bpm

import org.openrndr.animatable.easing.Easing

class Weight(private var w: Double = 0.0){

    data class Transition(val b: Double, val c: Double, val d: Double, val easing: Easing)

    private val transitions = mutableListOf<Transition>()

    /**
     * Push a transition to the queue.
     * This weight will transition from [b] to [c] over duration [d] (with optional easing [easing]).
     * @param b starting value (or null to infer seamless value)
     * @param c end value
     * @param d duration of transition in seconds
     * @param easing from [Easing] enum, default on None (linear)
     */
    fun pushTransition(
        b: Double? = null, // start value, null to infer seamless value
        c: Double, // end value
        d: Double, // duration
        easing: Easing = Easing.None // easing function
    ) {
        val _b = b ?: if (hasTransitions()) transitions.last().c else w
        transitions += Transition(_b, c, d, easing)
    }

    var t = 0.0 // time, relative to first transition in queue

    fun tick(deltaTime: Double) {
        if (hasTransitions()) t += deltaTime // Only tick if there are transitions to complete
        else t = 0.0 // Else, reset time
    }

    /**
     * Evaluate this weight. Right now.
     */
    fun eval() : Double {
        // Pop transitions that are out of duration
        while (hasTransitions() && t > transitions.first().d) {
            val completed = transitions.removeFirst()
            w = completed.c // Set weight to end value
            t -= completed.d // Reduce time by duration
        }
        // Eval first transition that is not completed
        if (hasTransitions()) {
            val uncompleted = transitions.first()
            w = uncompleted.run { easing.easer.ease(t, b, c, d) }
        }

        return w
    }

    /**
     * Clear all transitions and set weight to 0.0.
     */
    fun reset() {
        transitions.clear()
        w = 0.0
    }

    /**
     * Cancel all transitions.
     */
    fun cancelTransitions() {
        w = eval()
        transitions.clear()
    }

    /**
     * Returns if this weight has uncompleted transitions.
     */
    fun hasTransitions(): Boolean = (transitions.isNotEmpty())

}