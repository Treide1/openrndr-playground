package playground.minim

import ddf.minim.Minim
import ddf.minim.analysis.*
import org.openrndr.Fullscreen
import org.openrndr.KEY_ESCAPE
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.LIGHT_GRAY
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.minim.minim
import org.openrndr.extra.parameters.*
import org.openrndr.shape.LineSegment
import utils.*
import kotlin.math.pow

fun main() = application {
    configure {
        fullscreen = Fullscreen.CURRENT_DISPLAY_MODE
    }
    program {
        val minim = minim()
        val lineIn = minim.getLineIn(Minim.MONO, 2048/2, 48000f)
        val fft = FFT(lineIn.bufferSize(), lineIn.sampleRate())

        val windowList = FftWindow.values()
        var windowIndex = 0
        fun fftWindow(): FftWindow = windowList[windowIndex]
        fun setWindow() {
            windowIndex++
            windowIndex %= windowList.size

            fft.window(fftWindow().window)
        }

        // Drawer shorthands
        val lightGray = ColorRGBa.LIGHT_GRAY
        val soundRanges = SoundRanges.values()

        // GUI
        val gui = GUI()
        val settings = object {

            @DoubleParameter("avgOff", -1.0, 1.0)
            var avgOff: Double = 0.0

            @DoubleParameter("avgFac", eps, 10.0)
            var avgFac: Double = 1.0

            @BooleanParameter("isCorrected")
            var isCorrected = false
        }
        gui.add(settings, "Adapt avg")
        extend(gui)

        val allowLoggingAndWriting = false

        // CSV setup
        val pathToHere = System.getProperty("user.dir")+"/src/main/kotlin/playground/minim"
        val writer = CsvWriter("intensities.csv", pathToHere)
        val logger = Logger()
        writer.enabled = allowLoggingAndWriting
        writer.deleteFile()
        writer.write("ID, "+soundRanges.joinToString(", "), endOfLine = true)
        writer.idCounter = 0

        extend {
            fft.forward(lineIn.mix)

            logger.enabled = allowLoggingAndWriting && frameCount==60
            logger.log("")
            writer.enabled = allowLoggingAndWriting && frameCount==60

            val lowX = width*0.1
            val highX = width*0.9
            val lowY = height*0.9
            val highY = height*0.1
            val n = SoundRanges.values().size
            val barWidth = (highX-lowX)/n * 0.3

            // Draw Ranges (with center line)
            writer.write(writer.idCounter.toString())
            soundRanges.forEachIndexed { index, r ->
                logger.pushTag("$r")

                val lowF = r.freqRange.start.toFloat().alsoLog("lowF", logger)
                val highF =  r.freqRange.endInclusive.toFloat().alsoLog("highF", logger)
                // val midF = (lowF+highF)/2
                // val freqFac = highF/lowF

                val avg = fft.calcAvg(lowF, highF)
                // val avgDb = log10(avg)/10.0 // + settings.avgOff
                val intensity = avg
                intensity.alsoLog("intensity", logger).alsoWrite(
                    writer,index==soundRanges.indices.last
                ) { v -> "($index, $v)" }

                // Trial and error correction
                val scl = settings.avgFac
                val off = settings.avgOff
                val correctedIntensity = (avg /20 * 3.0.pow(index) * scl + off).smoothstep(0.0, 1.5)
                correctedIntensity.alsoLog("correctedIntensity", logger)

                val usedIntensity = if (!settings.isCorrected) intensity*1.0 else correctedIntensity
                val x = index.map(0, n, lowX, highX)
                val y = lowY.lerp(highY, usedIntensity)

                drawer.isolated {
                    val lineSegment = LineSegment(x, lowY, x, y)

                    stroke = ColorRGBa.WHITE
                    strokeWeight = barWidth
                    lineSegment(lineSegment)

                    stroke = lightGray
                    strokeWeight = 4.0
                    lineSegment(lineSegment)
                }

                logger.popTag()
            }

            // Debug overlay
            drawer.isolated {
                // Window usage text
                fill = ColorRGBa.GRAY
                text("Window: ${fftWindow().name} (cycle on w)", width*0.50 - 10.0, 24.0)

                // Intensity unit line and its label
                stroke = ColorRGBa.GRAY
                lineSegment(width*0.05, highY, width*0.95, highY)
                text("Intensity = 1", width*0.95-120.0, highY+16.0)
            }
        }

        keyboard.keyDown.listen {
            when(it.key) {
                KEY_ESCAPE -> application.exit()
            }
            when(it.name) {
                "w" -> setWindow()
            }
        }
    }
}


// Theory:
// https://housegrail.com/bass-treble-hertz-frequency-chart/
enum class SoundRanges(val freqRange: ClosedFloatingPointRange<Double>) {
    BASS(20.0 .. 160.0),
    MID_BASS(160.0 .. 320.0),
    MID(320.0 .. 2500.0),
    TREBLE(2500.0 .. 5000.0),
    BRILLIANCE(5000.0 .. 20000.0)
}

// Docs:
// https://code.compartmental.net/minim/javadoc/
enum class FftWindow(val window: WindowFunction) {
    LANCZOS(LanczosWindow()),
    HANN(HannWindow()),
    GAUSS(GaussWindow()),
    TRIANGULAR(TriangularWindow()) // calculates: 2 / l * (l / 2 - |i - (l - 1)| / 2));
}
