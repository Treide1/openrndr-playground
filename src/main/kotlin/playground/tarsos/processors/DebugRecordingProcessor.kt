package playground.tarsos.processors

import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.TarsosDSPAudioFormat
import be.tarsos.dsp.writer.WaveHeader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.RandomAccessFile

class DebugRecordingProcessor(val audioFormat: TarsosDSPAudioFormat, val output: RandomAccessFile): AudioProcessor {

    var audioLen = 0

    companion object {
        // Byte length of the header
        const val HEADER_LENGTH = 44
    }

    init {
        try {
            output.write(ByteArray(HEADER_LENGTH))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun process(audioEvent: AudioEvent): Boolean {
        try {
            audioLen += audioEvent.byteBuffer.size
            //write audio to the output
            output.write(audioEvent.byteBuffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return true
    }

    override fun processingFinished() {
        //write header and data to the result output
        val waveHeader = WaveHeader(
            WaveHeader.FORMAT_PCM,
            audioFormat.channels.toShort(),
            audioFormat.sampleRate.toInt(),
            16.toShort(),
            audioLen
        ) //16 is for pcm, Read WaveHeader class for more details
        val header = ByteArrayOutputStream()
        try {
            waveHeader.write(header)
            output.seek(0)
            output.write(header.toByteArray())
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

}
