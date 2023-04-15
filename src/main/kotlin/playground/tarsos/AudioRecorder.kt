package playground.tarsos

import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.math.IntVector2
import org.openrndr.math.clamp
import org.openrndr.math.map
import playground.tarsos.processors.RecordingProcessor
import playground.tarsos.processors.VolumeProcessor
import java.io.File

/**
 * Uses TarsosDSP to write audio to a WAV file.
 * Uses WaveformWriter to capture and write the audio.
 * Press SPACEBAR to start/stop recording.
 *
 * IMPORTANT: DO NOT USE. This is a proof of concept and does not work properly !
 * The recording assumes mono audio, but the audio is actually stereo.
 * The result is a WAV bursting your ears.
 */
fun main() = application {
    configure {
        width = 640
        height = 640
        position = IntVector2(0, 0)
        title = "OPENRNDR ~ TarsosDSP Audio Recorder"
    }
    program {

        // Tarsos setup for audio recording
        val sampleRate = 48000
        val bufferSize = 2048
        val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0)

        // Create the RecordingProcessor
        val cwd = File(System.getProperty("user.dir"))
        val writer = RecordingProcessor.create(cwd, listOf("audio", "test"), "recording.wav", dispatcher)

        // Create a volume processor to display the volume
        val volumeProcessor = VolumeProcessor(bufferSize)

        // Add the processors to the dispatcher
        dispatcher.addAudioProcessor(writer)
        dispatcher.addAudioProcessor(volumeProcessor)

        // Start the dispatcher
        Thread(dispatcher, "Audio Dispatcher").start()

        extend {
            // Display if recording or not with a small red dot in the upper corner
            drawer.fill = if (writer.isRecording) ColorRGBa.RED else ColorRGBa.BLACK
            drawer.stroke = ColorRGBa.WHITE
            drawer.circle(10.0, 10.0, 5.0)

            // Show the volume as a bar in the center of the screen
            drawer.fill = ColorRGBa.WHITE
            drawer.stroke = null
            val db = (volumeProcessor.volumeBuffer.lastOrNull() ?: -160.0).clamp(-160.0, 0.0)
            val x = width * 0.2
            val y = height / 2.0
            val w = width * 0.6 * db.map(-160.0, 0.0, 0.0, 1.0)
            val h = 10.0
            drawer.rectangle(x ,y, w, h)
        }

        fun terminate() {
            dispatcher.stop()
            application.exit()
        }

        // Toggle recording with KEY_SPACEBAR
        // Close the program with KEY_ESCAPE
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> terminate()
                KEY_SPACEBAR -> {
                    // Print application time
                    println("(Application time: ${application.seconds} sec.)")
                    writer.toggleRecording()
                }
            }
        }
    }
}