package playground

import org.openrndr.KEY_ESCAPE
import org.openrndr.KEY_SPACEBAR
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.*
import org.openrndr.math.Vector2
import org.openrndr.math.Vector4
import org.openrndr.resourceText
import org.openrndr.shape.Rectangle

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
        val fac = 0.9
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
            }

            // Draw the rect with black stroke, but no fill
            drawer.isolatedWithTarget(rt) {
                fill = null
                stroke = ColorRGBa.BLACK
                rectangle(mirrorFilter1.sourceRect)
                rectangle(mirrorFilter2.sourceRect)
            }

            // Perform the mirror filter
            mirrorFilter1.apply(drawBuffer, drawBuffer)
            mirrorFilter2.apply(drawBuffer, drawBuffer)

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
    cb: ColorBuffer,
    var srcX: Double = 0.0,
    var srcY: Double = 0.0,
    val srcW: Double,
    val srcH: Double,
    dst: Rectangle,
    val dims: Vector2 = Vector2(640.0, 480.0)
) : Filter(
    filterShaderFromCode(resourceText("/mirrorFromOther.glsl"), "mirrorFromOther")
) {
    var tex1: ColorBuffer by parameters
    var srcRect: Vector4 by parameters
    var dstRect: Vector4 by parameters
    var margin: Double by parameters

    init {
        tex1 = cb
        srcRect = getUvVector4()
        dstRect = Vector4(
            dst.x / dims.x,
            dst.y / dims.y,
            (dst.x + dst.width) / dims.x,
            (dst.y + dst.height) / dims.y
        )
        margin = 0.01
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

class CyclicFlag<T>(val options: List<T>) {
    var index = 0
    private var _value = options[index]

    val value: T
        get() = _value

    fun next() {
        index = (index + 1) % options.size
        _value = options[index]
    }
}