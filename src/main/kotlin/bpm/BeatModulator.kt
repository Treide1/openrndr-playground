package bpm

import org.openrndr.animatable.easing.Easing
import java.lang.IllegalArgumentException

const val MAX_ENVELOPES = 4

class BeatModulator : ClockSubscriber {

    /**
     * Array of nullable envelopes.
     * Access provides via [set] (as method or operator).
     * No direct read access to focus on range arithmetic.
     */
    private val envelopes = arrayOfNulls<BeatEnvelope?>(MAX_ENVELOPES)

    /**
     * Modulation weights for weighted sum of envelopes.
     * Can be subject of transitions, i.e. their value changes over time.
     */
    private val weights = Array(MAX_ENVELOPES) { Weight(0.0) }

    /**
     * Set the given [beatEnvelope] to the given index [i].
     * Its weight is initialized to 0.0
     *
     * To start using it in the modulation, call [pushTransition] with target values bigger than 0.0 .
     */
    operator fun set(i: Int, beatEnvelope: BeatEnvelope?) {
        if (i < 0 || i >= MAX_ENVELOPES) throw IllegalArgumentException("Index $i not allowed.")
        envelopes[i] = beatEnvelope
        weights[i].w = 0.0
    }

    override fun tick(seconds: Double, deltaTime: Double, frameCount: Int) {
        envelopes.forEach { env -> env?.tick(seconds, deltaTime, frameCount) }
        weights.forEach { weight -> weight.tick(deltaTime) }
    }

    /**
     * Synchronize all BeatEnvelopes added to this BeatModulator.
     * @param target The BeatEnvelope to sync to. If null, just syncs to 0.0.
     * @param phaseOff Offsets the phase to sync to. Adds on top of target phase.
     */
    fun syncAll(target: BeatEnvelope? = null, phaseOff: Double = 0.0) {
        val targetPhase = target?.phase ?: 0.0
        val offsetPhase = targetPhase + phaseOff
        envelopes.forEach { env -> env?.phase = offsetPhase}
    }

    @Deprecated("No benefit other keeping reference to beatEnvelope and sampling directly.")
    fun sampleList(envID: Int, phaseStart: Double, phaseEnd: Double, size: Int): List<Double> {
        if (envID < 0 || envID > MAX_ENVELOPES) throw IllegalArgumentException()
        return envelopes[envID]?.sampleList(phaseStart, phaseEnd, size) ?: List(size) { 0.0 }
    }

    /**
     * Sample all envelopes and calculate their weighted sum.
     * Samples are over the given phase from [phaseStart] to [phaseEnd] and have size [size].
     * @return List of weighted samples
     */
    fun sampleList(phaseStart: Double, phaseEnd: Double, size: Int): List<Double> {
        var weightedSumList = List(size) { 0.0 }
        envelopes.mapIndexed { i, env ->
            val w = weights[i].eval()
            weightedSumList = weightedSumList add env?.sampleList(phaseStart, phaseEnd, size)?.map { it*w }
        }
        return weightedSumList
    }

    /**
     * For the given HashMap [target], starts to transition the weights over the [duration].
     * Optional [easing] from [Easing] enum from standard-OPENRNDR animatable package.
     */
    fun pushTransition(target: HashMap<Int, Double>, duration: Double, easing: Easing = Easing.None) {
        for (i in 0..MAX_ENVELOPES) {
            if (target.containsKey(i)) {
                weights[i].pushTransition(weights[i].w, target[i]!!, duration, easing)
            }
        }
    }

    /**
     * Cancels all transitions, but keeps their last value as weights.
     */
    fun cancelAllTransitions() {
        weights.forEach { it.cancelTransitions() }
    }

}
