package external.templates.shadestyles

import org.openrndr.application
import org.openrndr.draw.*
import org.openrndr.shape.Circle
import kotlin.math.*

fun main() = application {
    program {
        val image = loadImage("data/images/cheeta.jpg")
        image.filter(MinifyingFilter.LINEAR_MIPMAP_NEAREST, MagnifyingFilter.LINEAR)
        extend {
            drawer.shadeStyle = shadeStyle {
                fragmentTransform = """
                        vec2 texCoord = c_boundsPosition.xy;
                        texCoord.y = 1.0 - texCoord.y;
                        vec2 size = textureSize(p_image, 0);
                        texCoord.x /= size.x/size.y;
                        x_fill = texture(p_image, texCoord);
                    """
                parameter("image", image)
            }

            val shape = Circle(width / 2.0, height / 2.0, 110.0).shape
            drawer.translate(cos(seconds) * 100.0, sin(seconds) * 100.0)
            drawer.shape(shape)
        }
    }
}
