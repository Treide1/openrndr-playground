package playground.tarsos.processors

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.ConstantQ
import be.tarsos.dsp.onsets.PercussionOnsetDetector
import be.tarsos.dsp.pitch.PitchProcessor
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm
import org.openrndr.math.saturate
import utils.lerp
import kotlin.math.abs

class MyPitchProcessor(
    algorithm: PitchEstimationAlgorithm?,
    sampleRate: Float,
    bufferSize: Int,
    val eventBufferSize: Int,
) : AudioProcessor {

    var lastPitch = LOWEST_PITCH

    // Pitch detection
    private val _pitchBuffer = mutableListOf(LOWEST_PITCH) // Pre-fill with "unpitched event"
    val pitchBuffer: List<Float>
        get() = _pitchBuffer.toList()

    // Frequency spectrum
    private val _spectrumBuffer = mutableListOf<List<Double>>()
    val spectrumBuffer: List<List<Double>>
        get() = _spectrumBuffer.toList()

    companion object {
        const val LOWEST_PITCH = 50.0f
        const val HIGHEST_PITCH = 350.0f
        val PITCH_RANGE = LOWEST_PITCH..HIGHEST_PITCH
    }

    // Internal pitch processor
    val internalPitchProcessor = PitchProcessor(algorithm, sampleRate, bufferSize) { pitchDetectionResult, _ ->
        val pitch = pitchDetectionResult.pitch
        val probability = pitchDetectionResult.probability
        val isPitched = pitchDetectionResult.isPitched

        val nextPitch = if (pitch !in PITCH_RANGE) lastPitch else pitch
        _pitchBuffer.add(nextPitch)
        if (_pitchBuffer.size > eventBufferSize) {
            _pitchBuffer.removeAt(0)
        }

        if (pitch in PITCH_RANGE && probability > 0.6 && isPitched && abs(lastPitch - pitch) > 0.01f) {
            lastPitch = pitch
        }

    }

    // ConstantQ
    val constantQ = ConstantQ(sampleRate, 160f, 5120f, 16f)
    val qBuffer = FloatArray(constantQ.ffTlength)
    var qBufferPos = 0
    var largestMag = 0.0000001 // eps to avoid division by zero

    // Percussion onset detection
    var percussionSalience = 0.0
    var lastTime = 0.0
    val maxDt = 0.4 // Charges the next percussion over time, reaching max at maxDt
    val handler = { time: Double, _: Double ->
        val dt = time - lastTime
        val fac = (dt/maxDt).saturate() * 0.5 + 0.5
        percussionSalience = percussionSalience.lerp(0.85, fac)
        lastTime = time
    }
    val percussionOnsetDetector = PercussionOnsetDetector(sampleRate, bufferSize, handler, 70.0, 8.0)

    override fun process(audioEvent: AudioEvent): Boolean {
        // Use the internal pitch processor to detect pitch
        internalPitchProcessor.process(audioEvent)

        // Use ConstantQ to get the frequency spectrum
        val floatBuffer = audioEvent.floatBuffer
        val start = 0
        val end = floatBuffer.size.coerceAtMost(qBuffer.size)
        floatBuffer.copyInto(qBuffer, destinationOffset = qBufferPos, startIndex = start, endIndex = end)
        qBufferPos += floatBuffer.size
        // If the qBuffer is full, calculate the magnitudes and add them to the spectrum buffer
        if (qBufferPos >= qBuffer.size) {
            constantQ.calculateMagintudes(qBuffer)
            _spectrumBuffer.add(constantQ.magnitudes.map { it.toDouble() })
            qBufferPos = 0
            largestMag = largestMag.coerceAtLeast(constantQ.magnitudes.max() * 1.0)
        }

        // Use the percussion onset detector to detect percussive events
        percussionOnsetDetector.process(audioEvent)
        percussionSalience *= 0.99

        return true
    }

    override fun processingFinished() {}
}