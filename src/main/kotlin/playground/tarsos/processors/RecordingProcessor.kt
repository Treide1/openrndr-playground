package playground.tarsos.processors

import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.writer.WriterProcessor
import java.io.File
import java.io.RandomAccessFile

class RecordingProcessor private constructor(f: File, val dispatcher: AudioDispatcher) : WriterProcessor(dispatcher.format, RandomAccessFile(f, "rw")) {

    var isRecording = false
    var startInSec = 0.0
    var stopInSec = 0.0

    /**
     * Toggle recording on/off.
     * Logs the current state to the console.
     * On recording stop, the WAV file is closed via super.processingFinished().
     */
    fun toggleRecording() {
        isRecording = !isRecording
        if (isRecording) {
            startInSec = dispatcher.secondsProcessed().toDouble()
            println("Recording started at $startInSec.")

        } else {
            stopInSec = dispatcher.secondsProcessed().toDouble()
            super.processingFinished()
            println("Recording stopped at $stopInSec. (Duration: ${stopInSec - startInSec} sec.)")
        }
    }

    override fun processingFinished() {
        // super.processingFinished()
        println("Recorder terminated.")
    }

    override fun process(audioEvent: AudioEvent?): Boolean {
        if (isRecording) {
            super.process(audioEvent)
        }
        return true
    }

    companion object {
        fun create(currentWorkingDir: File, pathToFile: List<String>, fileName: String, dispatcher: AudioDispatcher): RecordingProcessor {
            // File to write to
            val f = createFreshWavFile(currentWorkingDir, pathToFile, fileName)
            // Debug messages
            println("Recording to file: ${f.absolutePath}")
            println("Recording format: ${dispatcher.format}")
            // Create the RecordingProcessor
            return RecordingProcessor(f, dispatcher)
        }

        fun createFreshWavFile(currentWorkingDir: File, pathToFile: List<String>, fileName: String): File {

            // Recursively create directories if they don't exist
            // At the end, file is the path to the recording file
            var file = currentWorkingDir
            pathToFile.forEach {
                file = File(file, it)

                if (!file.exists()) {
                    file.mkdir()
                }
            }
            file = File(file, fileName)
            // Delete old file, if it exists.
            // Always create a new file.
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            return file
        }

    }
}