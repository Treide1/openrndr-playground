package playground.tarsos.processors

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import org.openrndr.math.clamp
import kotlin.math.log10

class VolumeProcessor(val bufferSize: Int): AudioProcessor {

    // Flag to single out the first event
    var isFirstEvent = true

    // Buffer to store the last decibel values
    private val _volumeBuffer = mutableListOf(LOWEST_RANGE) // Pre-fill with "silent event"
    val volumeBuffer: List<Double>
        get() = _volumeBuffer.toList()

    companion object {
        const val LOWEST_RANGE = -160.0
        const val HIGHEST_RANGE = 0.0
    }

    var loRange = LOWEST_RANGE
    var hiRange = HIGHEST_RANGE
    var rangeContraction = 0.98


    // Buffer to store dynamic range, as it changes over time
    private val _rangeBuffer = mutableListOf(loRange..hiRange) // Pre-fill with "silent range"
    val rangeBuffer: List<ClosedFloatingPointRange<Double>>
        get() = _rangeBuffer.toList()

    override fun process(audioEvent: AudioEvent): Boolean {
        // Calculate the root mean square (RMS) of the samples in the audio event
        val rms = audioEvent.floatBuffer.map { it * it }.average()
        // Convert the RMS to decibels
        val db = (20 * log10(rms)).clamp(-160.0, 0.0)
        // Add the decibel value to the volume buffer
        _volumeBuffer.add(db)
        // If the buffer is full, remove the oldest value
        if (_volumeBuffer.size > bufferSize) {
            _volumeBuffer.removeAt(0)
        }
        // Update range bounds
        if (isFirstEvent) {
            loRange = db
            hiRange = db
            isFirstEvent = false
        }
        loRange = if (db < loRange) db else loRange * rangeContraction + db * (1 - rangeContraction)
        hiRange = if (db > hiRange) db else hiRange * rangeContraction + db * (1 - rangeContraction)
        // Add the range to the range buffer
        _rangeBuffer.add(loRange..hiRange)
        // If the buffer is full, remove the oldest value
        if (_rangeBuffer.size > bufferSize) {
            _rangeBuffer.removeAt(0)
        }

        return true
    }

    override fun processingFinished() {}
}