package chrysanthemum

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.isolated
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import org.openrndr.math.map
import org.openrndr.shape.contour
import utils.toDegrees
import kotlin.math.PI
import kotlin.math.cos

/**
 * BeatEnvelope driven rings of flower petals.
 *
 * Switch between different envelopes highlighting beat patterns.
 *
 * Ideas for patterns:
 * 1-2-3-4- (Four on the floor)
 * --2---4- (Clap)
 * -+-+-+-+ (Off beat)
 * 1------- (Key beat)
 *
 * The four rings can be individually toggled, but not their individual petals.
 *
 * Author: Lukas Henke, 03.10.2022
 */
fun main() = application {
    configure {
        width = 1000
        height = 1000
        title = "Beat Petal Rings"
    }
    program {

        // Config
        fun contentSize() = 100.0

        fun symmetryNum() = 5
        fun petalPoints() = 40
        fun petalScl() = Vector2(2.5, 5.0)

        fun bpm() = 125.0 // Song: "Understatement" by "Solid Stone"
        fun startRad() = 20.0
        fun endRad() = 150.0

        // BEAT ENVELOPE

        val petalsPerRing = listOf(8, 12, 16, 20).reversed()

        //ROSE CURVE PETAL

        val petalContour = contour {
            // Aliases for easy reading
            val n = symmetryNum()
            val a = contentSize()
            val p = petalPoints()
            val arc = PI /n // angular width for each petal

            // Mapping index to contour point
            (0..p).forEach { i ->
                val theta = i.toDouble().map(0.0, p.toDouble(), -arc*.5, arc*.5)
                val r = a * cos(n*theta)

                val scl = petalScl()
                val v = Polar(theta.toDegrees(), r).cartesian.times(scl)
                moveOrLineTo(v)
            }
            close()
        }

        extend {
            petalsPerRing.forEachIndexed { i, petalAmount ->
                val relI = i.toDouble()/petalsPerRing.size
                (0 until petalAmount).forEach {j ->
                    drawer.isolated {
                        translate(width/2.0, height/2.0)

                        val theta = j*360.0/petalAmount
                        val rad = relI.map(0.0, 1.0, endRad(), startRad())
                        rotate(theta)
                        translate(rad, 0.0)

                        stroke = ColorRGBa.BLACK
                        fill = ColorRGBa.PINK.shade(relI*.5+.5)
                        contour(petalContour)
                    }
                }
            }

        }
    }
}