import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolatedWithTarget
import org.openrndr.draw.renderTarget
import org.openrndr.draw.shadeStyle
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.noise.random
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.shadestyles.LinearGradient
import org.openrndr.extra.shadestyles.RadialGradient
import org.openrndr.extra.triangulation.Delaunay
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.math.mix
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle
import org.openrndr.shape.ShapeContour
import org.openrndr.shape.offset
import kotlin.math.pow

/**
 * id: 2811db7a-ce55-4f1f-ad28-65e213a190fb
 *
 * description: A design using Delaunay / Voronoi. The starting point was how to place points in 2D space
 * for the tesselation in a way that it creates an interesting arrangement. I first sampled equidistant points
 * in a number of concentric circles.
 *
 * If the points in each circle are aligned, the result is very grid-like and not so interesting.
 * But if you apply a small rotation to the sampled points, more interesting and spiral-like results emerge.
 *
 * Next I experimented with uneven spacing between the circles, and between the points in each circle.
 * I think I also played with subtle randomization on the point's locations.
 *
 * Once I had the main shape I wondered how to make it less perfect, so I created a distortion force that is
 * stronger at the top border of the screen, which displaces the arranged shapes. The strength of the effect
 * is also used to alter the color and other properties.
 *
 * I often jumped back and forth between editing the arrangement and the rendering. I used 4 shaders: two built-in
 * ones (linear and radial gradient) and two custom ones: the first to color the lines segments in a way that only
 * the tips of the segments are visible and the centers transparent. The second one is a simple mathematical
 * pattern used to create shadows on the shapes and on the background. I do that by rendering things twice:
 * first the gradients, then the shadows on top.
 *
 * I did 90% without any GUI (no color selectors or sliders). The reason is that sometimes it feels like a distraction
 * to implement a gui. It feels like it's too early, when I'm still constantly moving code around and changing
 * the algorithm. Once I considered it was done I asked myself... if I add a GUI, can I come up with a better version?
 * And I think the answer is yes, because even if recompiling only takes a few seconds, moving a slider and seeing
 * multiple results per second allows to notice things which I would not notice when changing the code and running
 * the program again.
 *
 * I tried for the first time Vechro's OPENRNDR IntelliJ plugin, which shows color previews on the left
 * margin of the IDE for any line that creates a color. When clicking on them a color selector is presented. A bit
 * more convenient than typing RGB values. This already helped me fine tune the colors, but it still required
 * running the program after each change, so I took the last step, which was to create a GUI with over 20 sliders
 * and color selectors, so I could play with the numbers while the program runs.
 *
 * I was surprised to see that the program could animate (barely) the design while using the sliders,
 * even if it was doing a lot of computation. Somehow I had expected it would take one second to update after each
 * change.
 *
 * tags: #flower #spiral
 */

data class Cell(val contour: ShapeContour, val color0: ColorRGBa, val color1: ColorRGBa, val amt: Double)

fun main() {
    application {
        configure {
            width = 1200
            height = 1200
        }
        program {
            val gui = GUI().also {
                it.compartmentsCollapsedByDefault = false
                it.persistState = true
            }
            val rt = renderTarget(width, height) {
                colorBuffer()
                depthBuffer()
            }

            val cfg = object {
                @DoubleParameter("angle", 0.0, 30.0, order = 5)
                var angle = 5.0

                @ColorParameter("tint", order = 10)
                var color2 = ColorRGBa.fromHex("#da6e5b")

                @ColorParameter("lines", order = 15)
                var colorLines = ColorRGBa.fromHex("#f8c5a5")

                @DoubleParameter("radius pow", 0.5, 3.0, order = 20)
                var radiusPow = 1.0

                @DoubleParameter("angular pow", 0.5, 3.0, order = 25)
                var angularPow = 2.5

                @IntParameter("inner point count", 1, 30, order = 30)
                var innerPointCount = 5

                @IntParameter("outer point count", 1, 30, order = 35)
                var outerPointCount = 24

                @DoubleParameter("bg angle", 0.0, 30.0, order = 40)
                var bgAngle = 20.0

                @DoubleParameter("bg lum", 0.0, 1.0, order = 45)
                var bgLum = 0.10

                @DoubleParameter("bg scale", 0.001, 0.050, order = 50)
                var bgScale = 0.002

                @DoubleParameter("cell angle", 0.0, 1.0, order = 55)
                var cellAngle = 0.13

                @DoubleParameter("cell scale", 0.001, 0.050, order = 60)
                var cellScale = 0.015

                @DoubleParameter("y pos", 0.0, 1.0, order = 70)
                var yPos = 0.55

                @DoubleParameter("damageRot", 0.0, 90.0, order = 80)
                var damageRot = 30.0

                @DoubleParameter("damageOffest", 0.0, 200.0, order = 90)
                var damageOffset = 100.0

                @DoubleParameter("shape radius", 0.4, 0.8, order = 100)
                var radius = 0.46
            }

            val cfgLinearGradient = object {
                @ColorParameter("color 0")
                var color0 = ColorRGBa.fromHex("#567e28")

                @ColorParameter("color 1")
                var color1 = ColorRGBa.fromHex("#033413")

                @DoubleParameter("exp", 0.5, 3.0)
                var exponent = 1.2
            }

            val layers = 18

            // Shaders
            val cornerGlow = shadeStyle {
                fragmentTransform = """
                    float c = pow(abs(2.0 * c_contourPosition / p_len - 1.0), 3.0);
                    x_stroke.rgb = mix(x_stroke.rgb, vec3(0.8, 1.0, 0.6), c * c * 0.2);
                    x_stroke.a *= c;
                """.trimMargin()
            }

            val stripes = shadeStyle {
                fragmentTransform = """
                    vec2 pos = va_position.xy * p_scale + p_angle;
                    float l = fract(
                        sin(pos.y * (1.0 + 0.1 * sin(pos.y)) + 
                        sin(pos.x * (1.0 + 0.1 * sin(pos.y)))));
                    float l2 = smoothstep(0.2, 0.3, l) - smoothstep(0.7, 0.95, l);
                    x_fill.rgb = vec3(p_amt);
                    x_fill.a *= l2 * 0.1;
                """.trimMargin()
            }
            val radialGradient = RadialGradient(
                ColorRGBa.fromHex("#102026"), ColorRGBa.fromHex("#535a5e"), exponent = 1.3, length = 0.7
            )
            val linearGradient = LinearGradient(
                cfgLinearGradient.color0, cfgLinearGradient.color0, exponent = cfgLinearGradient.exponent
            )

            fun render() {
                // Shapes and points
                val circle = Circle(Vector2.ZERO, width * cfg.radius)
                val points = List(layers) { layerNum ->
                    val t = layerNum / (layers - 1.0)
                    val tt = t.pow(cfg.radiusPow)
                    val radius = circle.radius * tt.map(0.0, 1.0, 0.1, 0.9)
                    val inner = circle.contour.offset(-radius) // don't grow too small
                        .transform(transform { rotate(layerNum * cfg.angle) }) // play with rotation
                    val numPoints = mix(cfg.outerPointCount * 1.0, cfg.innerPointCount * 1.0, t).toInt()
                    List(numPoints) { pNum -> inner.position((pNum.toDouble() / numPoints).pow(cfg.angularPow)) }
                }.flatten().filter { circle.contains(it) }

                // Generate contours
                val delaunay = Delaunay.from(points)
                val voronoi =
                    delaunay.voronoi(drawer.bounds.movedBy(-drawer.bounds.center))

                // Drop outer contours
                val cells = voronoi.cellsPolygons()
                    .filter { contour -> contour.segments.all { seg -> circle.contains(seg.start) } }

                // Displaced and rotated contours with color and distortion amount templates.data
                val cells2 = cells.mapIndexed { i, it ->
                    val y = it.bounds.center.y
                    val amt = y.map(-200.0, -50.0, 1.0, 0.0, true)
                    val mat = transform {
                        translate(it.bounds.center)
                        rotate(amt * random(-cfg.damageRot, cfg.damageRot))
                        translate(-it.bounds.center - Vector2(0.0, cfg.damageOffset * amt))
                    }
                    Cell(
                        it.transform(mat),
                        cfgLinearGradient.color0.mix(cfg.color2, amt),
                        cfgLinearGradient.color1.shade(0.8 + (i % 2) * 0.3),
                        amt
                    )
                }

                drawer.isolatedWithTarget(rt) {
                    // Background gradient
                    shadeStyle = radialGradient
                    stroke = null
                    rectangle(bounds)

                    // Background shadows
                    stripes.parameter("angle", cfg.bgAngle)
                    stripes.parameter("amt", cfg.bgLum)
                    stripes.parameter("scale", cfg.bgScale)
                    shadeStyle = stripes
                    contour(bounds.contour)

                    translate(bounds.position(0.5, cfg.yPos))
                    cells2.forEachIndexed { i, cell ->
                        // Cell gradient
                        stroke = null
                        linearGradient.color0 = cell.color0
                        linearGradient.color1 = cell.color1
                        linearGradient.rotation = i.toDouble()
                        linearGradient.exponent = cfgLinearGradient.exponent
                        shadeStyle = linearGradient
                        contour(cell.contour)

                        // Cell shadows
                        shadeStyle = stripes
                        stripes.parameter("angle", i * cfg.cellAngle)
                        stripes.parameter("amt", cell.amt)
                        stripes.parameter("scale", cfg.cellScale)
                        contour(cell.contour)

                        // Cell borders
                        shadeStyle = cornerGlow
                        stroke = cfg.colorLines
                        cell.contour.segments.forEach { seg ->
                            cornerGlow.parameter("len", seg.length)
                            segment(seg)
                        }
                    }
                }
            }

            render()
            gui.onChange { _, _ -> render() }
            gui.add(cfg, "Config")
            gui.add(cfgLinearGradient, "Linear Gradient")
            gui.add(radialGradient, "Radial gradient")

            extend(gui)
            extend {
                drawer.image(rt.colorBuffer(0))
            }
        }
    }
}