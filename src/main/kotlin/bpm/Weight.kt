package bpm

import org.openrndr.animatable.easing.Easing

class Weight(var w: Double = 0.0){

    data class Transition(val b: Double, val c: Double, val d: Double, val easing: Easing)

    private val transitions = mutableListOf<Transition>()

    fun pushTransition(
        b: Double, // start value
        c: Double, // end value
        d: Double, // duration
        easing: Easing = Easing.None // easing function
    ) {
        transitions += Transition(b, c, d, easing)
    }

    var t = 0.0 // time, relative to first transition in queue

    fun tick(deltaTime: Double) {
        if (hasTransitions()) t += deltaTime // Only tick if there are transitions to complete
        else t = 0.0 // Else, reset time
    }

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

    fun cancelTransitions() {
        w = eval()
        transitions.clear()
    }

    fun hasTransitions(): Boolean = (transitions.isNotEmpty())
}