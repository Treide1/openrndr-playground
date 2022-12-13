package envelopeCapture

import org.openrndr.Extension
import org.openrndr.Mouse
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.extra.color.presets.DARK_RED
import org.openrndr.math.Vector2

/**
 * Class to capture and convert mouse movement easily.
 *
 * Use as an Extension and [start] and [stop] the capturing.
 * Define [onCaptureStarted], [onCaptured] and [onCaptureStopped] in one go.
 * You can access the [captureEvents] directly.
 */
class MouseCapture : Extension {

    /**
     * Data class of single mouse capture.
     * @param t Time of capture, relative to start of capturing. Calling [start] resets this to 0.
     * @param pos Mouse position being captured in screen-absolute xy coordinates.
     */
    data class CaptureEvent(val t: Double, val pos: Vector2)

    override var enabled = true

    /**
     * If true, shows a capture symbol instead of the mouse cursor.
     */
    var isUsingRecordCursor = true

    var onCaptureStarted : MouseCapture.() -> Unit = {}
    var onCaptured : MouseCapture.() -> Unit = {}
    var onCaptureStopped : MouseCapture.() -> Unit = {}

    var isCapturing = false
        private set
    private var timeSinceCapturing = 0.0
    var captureLength = 0.0


    /**
     * Contains every [CaptureEvent] of this current or latest concluded capturing in chronological order.
     *
     * Calling [start] clears this list.
     */
    val captureEvents = mutableListOf<CaptureEvent>()

    lateinit var mouse: Mouse

    override fun setup(program: Program) {
        super.setup(program)
        mouse = program.mouse
    }

    /**
     * Overrides [beforeDraw] from [Extension].
     * Does the capture if isCapturing is on.
     * Also performs the [onCaptured] block.
     */
    override fun beforeDraw(drawer: Drawer, program: Program) {
        if (isCapturing) {
            val pos = mouse.position

            timeSinceCapturing += program.deltaTime
            val t = timeSinceCapturing

            if (t > captureLength) {
                isCapturing = false
                return
            }

            captureEvents.add(CaptureEvent(t, pos))

            this.onCaptured()
        }
    }

    override fun afterDraw(drawer: Drawer, program: Program) {
        if (isCapturing && isUsingRecordCursor) {
            drawer.isolated {

                // TODO: Filling circle over time, proportional filling over angle 0.0 -> 360.0
                fill = ColorRGBa.RED
                stroke = ColorRGBa.DARK_RED
                circle(mouse.position, 10.0)
            }

        }
    }

    /**
     * Starts the capturing. This frames' capture will be excluded.
     *
     * First capture will happen on the next frame, but the time starts this frame.
     * Thus, every [CaptureEvent] will have [CaptureEvent.t] > 0.
     */
    fun start() {
        isCapturing = true
        timeSinceCapturing = 0.0
        captureEvents.clear()

        if (isUsingRecordCursor) mouse.cursorVisible = false

        this.onCaptureStarted()
    }

    /**
     * Stops the capturing. This frames' capture will still be included.
     */
    fun stop() {
        isCapturing = false
        if (isUsingRecordCursor) mouse.cursorVisible = true

        this.onCaptureStopped()
    }

}

/**
 * Given a bpm and a count of beats, returns the time in seconds this many beats take.
 */
fun beatsToSeconds(beatCount: Int, bpm: Double) : Double = (60.0 / bpm) * beatCount