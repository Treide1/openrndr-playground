package playground.minim

import ddf.minim.AudioInput
import ddf.minim.AudioListener
import ddf.minim.Minim
import ddf.minim.analysis.BeatDetect
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.minim.minim
import org.openrndr.math.Vector2
import kotlin.math.pow

fun main() = application {
    configure { }
    program {
        val minim = minim()
        val lineIn = minim.getLineIn(Minim.MONO, 2048, 48000f)
        val beat = BeatDetect(lineIn.bufferSize(), lineIn.sampleRate())
        beat.setSensitivity(300)
        BeatListener.createAndBind(beat, lineIn)

        var kickVal = 1.0
        var snareVal = 1.0
        var hatVal = 1.0

        extend {
            kickVal =   if (beat.isKick)    1.0 else (kickVal * 0.95).coerceAtLeast(0.1)
            snareVal =  if (beat.isSnare)   1.0 else (snareVal * 0.95).coerceAtLeast(0.1)
            hatVal =    if (beat.isHat)     1.0 else (hatVal * 0.95).coerceAtLeast(0.1)

            val colByValue = { value: Double -> ColorRGBa.PINK.shade(1-value.pow(2)).toRGBa()}

            val dim = drawer.bounds.dimensions
            val kickCenter = dim * Vector2(0.25, 0.50)
            val snareCenter = dim * Vector2(0.50, 0.50)
            val hatCenter = dim * Vector2(0.70, 0.50)
            val textOff = Vector2(5.0, -5.0)

            drawer.isolated {
                fill = colByValue(kickVal)
                rectangle(kickCenter, 40.0, -20.0)
                fill = ColorRGBa.BLACK
                text("KICK", kickCenter + textOff)

                fill = colByValue(snareVal)
                rectangle(snareCenter, 48.0, -20.0)
                fill = ColorRGBa.BLACK
                text("SNARE", snareCenter + textOff)

                fill = colByValue(hatVal)
                rectangle(hatCenter, 32.0, -20.0)
                fill = ColorRGBa.BLACK
                text("HAT", hatCenter + textOff)
            }
        }
    }
}

class BeatListener(private val beat: BeatDetect, private val source: AudioInput) : AudioListener {

    override fun samples(p0: FloatArray?) {
        beat.detect(source.mix)
    }

    override fun samples(p0: FloatArray?, p1: FloatArray?) {
        beat.detect(source.mix)
    }

    companion object {
        fun createAndBind(beat: BeatDetect, source: AudioInput) {
            source.addListener(
                BeatListener(beat, source)
            )
        }
    }
}