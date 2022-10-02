package utils

import org.openrndr.color.ColorRGBa
import org.openrndr.draw.Drawer
import org.openrndr.draw.isolated
import org.openrndr.math.map
import org.openrndr.shape.Rectangle
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow


fun getTupleIndex(item: String, map: List<List<String>>): List<Int>? {
    map.forEachIndexed { i, list ->
        list.forEachIndexed { j, s ->
            if (item == s) return listOf(i, j)
        }
    }
    return null
}

fun String.splitByLength(length: Int = 1): List<String> {
    val chars = this.toCharArray()
    if (length == 1) return chars.map { c -> c.toString() }

    val buffer = mutableListOf<Char>()
    val result = mutableListOf<String>()

    chars.forEachIndexed { i, c ->
        buffer.add(c)
        if (i % length == 0) result.add( buffer.joinToString("") )
    }
    return result
}

/**
 * Hull curve on [edge0, edge1], rescaled to [0,1]
 * Values are 0 before domain, and 1 after domain.
 * Smoothly transitions from (0,0) to (1,1) with famous smoothstep function.
 *
 */
fun Double.smoothstep( edge0: Double, edge1: Double): Double {
    val t = this.map(edge0, edge1, 0.0, 1.0)
    return when {
        t < 0.0 -> 0.0
        t > 1.0 -> 1.0
        else -> 3*t.pow(2) - 2*t.pow(3)
    }
}

/**
 * Hull curve on [0,1] with 0 elsewhere.
 * Linear segments from (0,0) to (.5, 1) to (1, 0), forming a spike.
 */
fun hullSpike(t: Double): Double = when {
    t < 0.0 || t > 1.0  -> 0.0
    t < 0.5             -> t*2.0
    else                -> 2.0 - t*2.0
}

fun Double.toDegrees(): Double{
    return this.map(0.0, 2* PI, 0.0, 360.0)
}

/**
 * Get a sublist, where the indices 'from' and 'to' are allowed to be any integer.
 * The list indices wraps around below 0 and above its size, as if the input list in circular.
 *
 * Returned sublist includes element at 'from' and excludes element at 'to', even in backwards direction.
 */
fun <T> List<T>.circularSublist(from: Int, to: Int): List<T> {

    var a = from
    var b = to

    // If 'from' is after 'to',
    // then call this fun with reversed order.
    // Left inclusive, right exclusive call.
    // So [from: 0, to: -3] -> range: (0, -1, -2)
    // with from = to + 1 and to = from + 1
    // becomes [from: -2, to: 1] -> (-2, -1, 0)
    if (a > b) {
        val tmp = a
        a = b + 1
        b = tmp + 1
    }
    // Assume: a < b

    // Take the kotlin-specific negative version first.
    // Division and Modulo work on negative numbers as if negation was a weaker operator.
    // E.g. -3 mod 4 is treated like
    //  -3 % 4 = -(3 % 4) = -(3) = -3
    // We intentionally overshoot. Min overshoot: 1, max overshoot: total
    val total = this.size
    if (a < 0) {
        val lift = abs(a / total + 1) * total
        a += lift
        b += lift
    }
    // Assume: a at least 0 ^ a < b
    // Thus: [a, b) is subset of 0-inclusive naturals

    // If the nextTotal is less than b, than we cut the range [a,b) at nextTotal.
    // We concat the recursive call on [a, nextTotal) + [nextTotal,b)
    val diffToNextTotal = total - (a % total)
    val nextTotal = a + diffToNextTotal
    if (nextTotal < b) {
        return circularSublist(a, nextTotal) + circularSublist(nextTotal, b)
    }
    // Assume: nextTotal >= b ^ [a, b) is positive interval
    // Thus: [a, b) is subset of [0, nextTotal)

    // Recursion stops for returning the canonical sublist.
    val fall = a - (a%total)
    return subList(a - fall, b - fall)

}


fun Drawer.showCoordinateSystem(scl: Double) {

    val axis = Rectangle(-scl, 0.0, scl*2, 1.0)
    this.isolated {
        stroke = ColorRGBa.RED
        this.rectangle(axis)
        this.rotate(90.0)
        stroke = ColorRGBa.BLUE
        this.rectangle(axis)
    }

}