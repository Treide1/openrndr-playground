package bpm

import org.openrndr.math.map
import java.lang.IllegalArgumentException

const val BEAT_ENVELOPES_PER_MODULATOR = 4

class BeatModulator : ClockSubscriber {

    /**
     * Array of nullable envelopes.
     * Access provides via [set] (as method or operator).
     * No direct read access to focus on range arithmetic.
     */
    private val envelopes = arrayOfNulls<BeatEnvelope?>(BEAT_ENVELOPES_PER_MODULATOR)

    operator fun set(i: Int, beatEnvelope: BeatEnvelope?) {
        if (i < 0 || i >= BEAT_ENVELOPES_PER_MODULATOR) throw IllegalArgumentException("Index $i not allowed.")
        envelopes[i] = beatEnvelope
    }

    override fun tick(seconds: Double, deltaTime: Double, frameCount: Int) {
        envelopes.forEach { env -> env?.tick(seconds, deltaTime, frameCount) }
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

    fun sampleList(envID: Int, phaseStart: Double, phaseEnd: Double, size: Int): List<Double> {
        if (envID < 0 || envID > BEAT_ENVELOPES_PER_MODULATOR) throw IllegalArgumentException()
        return envelopes[envID]?.sampleList(phaseStart, phaseEnd, size) ?: List(size) { 0.0 }
    }

}
