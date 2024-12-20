package playground

import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.extra.shapes.grid
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4
import org.openrndr.resourceText
import org.openrndr.shape.Rectangle
import utils.CyclicFlag

/**
 * Demonstrate a milkdrop style fractal by using a "mirror".
 * A mirror is a portion of the screen that derives its pixels from another screen portion.
 * The size of the mirror does not need to match with the target portion,
 * i.e. it allows for scaling, as well as rotation, shift and blending.
 *
 * The simplest non-trivial case are two rectangles that are mirroring a slightly smaller center rect.
 *
 * Tries to imitate: Project M - Milkdrop 2.0 - "Brain coral cartoon" by 'shifter'
 */
fun main() = application {
    configure {
        // 640x480 config
        width = 640
        height = 480
    }
    program {
        // Init render target to draw on
        val rt = renderTarget(width, height) {
            colorBuffer()
        }
        val drawBuffer = rt.colorBuffer(0)

        // Init color buffer for the last frame
        val lastFrame = drawBuffer.createEquivalent()

        // Init single mirror filter
        val sourceW = 150.0
        val sourceH = 100.0
        val fac = 0.95
        val dW = sourceW * fac
        val dH = sourceH * fac
        val targetRect = Rectangle(300.0, 200.0, dW, dH)
        val mirrorFilter1 = MirrorFilter(lastFrame, 0.0, 0.0, sourceW, sourceH, targetRect)
        val mirrorFilter2 = MirrorFilter(lastFrame, 0.0, 0.0, sourceW, sourceH, targetRect)

        // Init colors
        val red = ColorRGBa.RED
        val blue = ColorRGBa.BLUE

        // Runtime var
        val activeMirror = CyclicFlag(listOf(mirrorFilter1, mirrorFilter2))

        fun drawMirror(mirror: MirrorFilter) {
            mirror.apply(drawBuffer, drawBuffer)

            // Draw the mirror's source rect
            drawer.isolatedWithTarget(rt) {
                fill = null
                stroke = ColorRGBa.BLACK
                rectangle(mirror.sourceRect)
            }
        }

        extend {
            // Update mouse position to the mirror filter
            activeMirror.value.updateMousePosForSrc(mouse.position)

            // Draw the upper half of the screen red, the lower half blue
            drawer.isolatedWithTarget(rt) {
                clear(ColorRGBa.TRANSPARENT)
                stroke = null
                fill = red
                rectangle(0.0, 0.0, width.toDouble(), height / 2.0)
                fill = blue
                rectangle(0.0, height / 2.0, width.toDouble(), height / 2.0)

                bounds.grid(5, 5).flatten().forEach { r ->
                    stroke = ColorRGBa.WHITE
                    strokeWeight = 0.5
                    fill = null
                    rectangle(r)
                }
            }

            // Perform the mirror filter
            drawMirror(mirrorFilter1)
            drawMirror(mirrorFilter2)

            // Draw the render targets color buffer to the screen
            drawer.image(drawBuffer)

            // Copy the render target's color buffer to the last frame's color buffer
            drawBuffer.copyTo(lastFrame)
        }

        // On ESCAPE, exit the program
        keyboard.keyDown.listen {
            when (it.key) {
                KEY_ESCAPE -> application.exit()
                KEY_SPACEBAR -> activeMirror.next()
            }
        }
    }
}

// Filter class for mirroring a portion of the screen from the last screen frame
class MirrorFilter(
    cb: ColorBuffer, // Other-mirroring only
    var srcX: Double = 0.0,
    var srcY: Double = 0.0,
    val srcW: Double,
    val srcH: Double,
    dst: Rectangle,
    val dims: Vector2 = Vector2(640.0, 480.0)
) : Filter(
    filterShaderFromCode(
        // Try out mirroring from another buffer, requires another color buffer as input
        //resourceText("/mirrorFromOther.glsl"), "mirrorFromOther"
        // Try out self mirroring
        resourceText("/mirrorFromSelf.glsl"), "mirrorFromSelf"
    )
) {
    // var tex1: ColorBuffer by parameters // Other-mirroring only
    var srcRect: Vector4 by parameters
    var dstRect: Vector4 by parameters

    init {
        // tex1 = cb
        srcRect = getUvVector4()
        dstRect = Vector4(
            dst.x / dims.x,
            dst.y / dims.y,
            (dst.x + dst.width) / dims.x,
            (dst.y + dst.height) / dims.y
        )
    }

    fun getUvVector4(): Vector4 {
        val yVals = listOf(1.0 - srcY / dims.y, 1.0 -(srcY + srcH) / dims.y)
        return Vector4(
            srcX / dims.x,
            yVals.min(),
            (srcX + srcW) / dims.x,
            yVals.max(),
        )
    }

    val sourceRect: Rectangle
        get() = Rectangle(srcX, srcY, srcW, srcH)

    fun updateMousePosForSrc(mousePos: Vector2) {
        srcX = mousePos.x
        srcY = mousePos.y
        srcRect = getUvVector4()
    }
}

