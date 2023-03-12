package external.templates.noise

//import org.openrndr.application
//import org.openrndr.color.rgb
//import org.openrndr.draw.colorBuffer
//import org.openrndr.extra.noise.Random
//import org.openrndr.extra.noise.filters.*
//import org.openrndr.math.Vector3
//import org.openrndr.math.Vector4
//import kotlin.math.sin
//
///**
// * A sine oscillator with randomized parameters
// */
//class SinOsc {
//    private val freq = Random.double(0.1, 2.0)
//    private val phase = Random.double(0.0, 6.28)
//    private val add = Random.double(0.0, 1.0)
//    private val mul = Random.double(0.0, 1.0 - add)
//    operator fun invoke() = sin(System.currentTimeMillis() * 0.0001 * freq + phase) * mul + add
//}
//
///**
// * Render an animated Simplex3D texture using shaders.
// *
// * The uniforms in the shader are controlled by
// * randomized sine oscillators.
// */
//fun main() = application {
//    program {
//        val noise = SimplexNoise3D()
//        fun <T> List<T>.windowedCircular(size: Int, step: Int = 1): List<List<T>> {
//            return this.windowed(size, step, partialWindows = true).map { window ->
//                val diff = size - window.size
//                if (diff == 0) { window }
//                else { window + this.subList(0, diff) }
//            }
//        }
//        val img = colorBuffer(width, height)
//        val wav = List(21) { SinOsc() }
//
//        extend {
//            noise.seed = Vector3(wav[0](), wav[1](), wav[2]()) // = position
//            noise.scale = Vector3(wav[3](), wav[4](), wav[5]())
//            noise.lacunarity = Vector3(wav[6](), wav[7](), wav[8]())
//            noise.gain = Vector4(wav[9](), wav[10](), wav[11](), wav[12]())
//            noise.decay = Vector4(wav[13](), wav[14](), wav[15](), wav[16]())
//            noise.octaves = 4
//            noise.bias = Vector4(wav[17](), wav[18](), wav[19](), wav[20]())
//
//            noise.apply(emptyArray(), img)
//            drawer.clear(rgb(0.20, 0.18, 0.16))
//            drawer.image(img)
//        }
//    }
//}
//
//private const val F3 = (1.0 / 3.0).toFloat()
//private const val G3 = (1.0 / 6.0).toFloat()
//private const val G33 = G3 * 3 - 1
//
//fun simplex(seed: Int, position: Vector3): Double = simplex(seed, position.x, position.y, position.z)
//
//fun simplex(seed: Int, x: Double, y: Double, z: Double): Double {
//
//    val t = (x + y + z) / 3.0
//    val i = (x + t).fastFloor()
//    val j = (y + t).fastFloor()
//    val k = (z + t).fastFloor()
//
//    val t2 = (i + j + k) / 6.0
//    val x0 = x - (i - t2)
//    val y0 = y - (j - t2)
//    val z0 = z - (k - t2)
//
//    val i1: Int
//    val j1: Int
//    val k1: Int
//
//    val i2: Int
//    val j2: Int
//    val k2: Int
//
//    if (x0 >= y0) {
//        when {
//            y0 >= z0 -> {
//                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
//            x0 >= z0 -> {
//                i1 = 1; j1 = 0; k1 = 0; i2 = 1; j2 = 0; k2 = 1; }
//            else -> {
//                i1 = 0; j1 = 0; k1 = 1; i2 = 1; j2 = 0; k2 = 1; }
//        }
//    } else {
//        when {
//            y0 < z0 -> {
//                i1 = 0; j1 = 0; k1 = 1; i2 = 0; j2 = 1; k2 = 1; }
//            x0 < z0 -> {
//                i1 = 0; j1 = 1; k1 = 0; i2 = 0; j2 = 1; k2 = 1; }
//            else -> {
//                i1 = 0; j1 = 1; k1 = 0; i2 = 1; j2 = 1; k2 = 0; }
//        }
//    }
//    val x1 = x0 - i1 + 1.0 / 6.0
//    val y1 = y0 - j1 + 1.0 / 6.0
//    val z1 = z0 - k1 + 1.0 / 6.0
//    val x2 = x0 - i2 + 1.0 / 3.0
//    val y2 = y0 - j2 + 1.0 / 3.0
//    val z2 = z0 - k2 + 1.0 / 3.0
//    val x3 = x0 + G33
//    val y3 = y0 + G33
//    val z3 = z0 + G33
//
//    val n0: Double
//    run {
//        var lt = 0.6 - x0 * x0 - y0 * y0 - z0 * z0
//        if (lt < 0) {
//            n0 = 0.0
//        } else {
//            lt *= lt
//            n0 = lt * lt * gradCoord3D(seed, i, j, k, x0, y0, z0)
//        }
//    }
//    val n1: Double
//    run {
//        var lt = 0.6 - x1 * x1 - y1 * y1 - z1 * z1
//        if (lt < 0) {
//            n1 = 0.0
//        } else {
//            lt *= lt
//            n1 = lt * lt * gradCoord3D(seed, i + i1, j + j1, k + k1, x1, y1, z1)
//        }
//    }
//    val n2: Double
//    run {
//        var lt = 0.6 - x2 * x2 - y2 * y2 - z2 * z2
//        if (lt < 0) {
//            n2 = 0.0
//        } else {
//            lt *= lt
//            n2 = lt * lt * gradCoord3D(seed, i + i2, j + j2, k + k2, x2, y2, z2)
//        }
//    }
//
//    val n3: Double
//    run {
//        var lt = 0.6 - x3 * x3 - y3 * y3 - z3 * z3
//        if (lt < 0)
//            n3 = 0.0
//        else {
//            lt *= lt
//            n3 = lt * lt * gradCoord3D(seed, i + 1, j + 1, k + 1, x3, y3, z3)
//        }
//    }
//    return 32 * (n0 + n1 + n2 + n3)
//}
//
//fun Vector3.Companion.simplex(seed: Int, x: Double): Vector3 = Vector3(simplex(seed, x, 0.0, 0.0),
//    simplex(seed, 0.0, x + 31.3383, 0.0),
//    simplex(seed, 0.0, 0.0, x - 483.23))