package playground.keyframer.json

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*


open class AnimationParser {

    @OptIn(ExperimentalSerializationApi::class)
    fun build(block: JsonArrayBuilder.() -> Unit) : String {
        val array = buildJsonArray {
            block()
        }

        val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
        return json.encodeToString(array)
    }

    fun JsonArrayBuilder.step(time: Double, easing: Easing? = null, block: JsonObjectBuilder.() -> Unit) {
        this.addJsonObject {
            put("time", time)
            put("easing", easing?.value ?: Easing.LINEAR.value)
            block()
        }
    }

    infix fun <T> T.via(other: Easing) : JsonObject {
        return buildJsonObject {
            put("easing", other.value)
            when (this@via) {
                is Number -> put("value", this@via)
                else -> put("value", this@via.toString())
            }

        }
    }

    enum class Easing(val value: String) {
        LINEAR("linear"),
        BACK_IN("back-in"),
        BACK_OUT("back-out"),
        BACK_IN_OUT("back-in-out"),
        BOUNCE_IN("bounce-in"),
        BOUNCE_OUT("bounce-out"),
        BOUNCE_IN_OUT("bounce-in-out"),
        CIRC_IN("circ-in"),
        CIRC_OUT("circ-out"),
        CIRC_IN_OUT("circ-in-out"),
        CUBIC_IN("cubic-in"),
        CUBIC_OUT("cubic-out"),
        CUBIC_IN_OUT("cubic-in-out"),
        ELASTIC_IN("elastic-in"),
        ELASTIC_OUT("elastic-out"),
        ELASTIC_IN_OUT("elastic-in-out"),
        EXPO_IN("expo-in"),
        EXPO_OUT("expo-out"),
        EXPO_IN_OUT("expo-in-out"),
        QUAD_IN("quad-in"),
        QUAD_OUT("quad-out"),
        QUAD_IN_OUT("quad-in-out"),
        QUART_IN("quart-in"),
        QUART_OUT("quart-out"),
        QUART_IN_OUT("quart-in-out"),
        QUINT_IN("quint-in"),
        QUINT_OUT("quint-out"),
        QUINT_IN_OUT("quint-in-out"),
        SINE_IN("sine-in"),
        SINE_OUT("sine-out"),
        SINE_IN_OUT("sine-in-out"),
        ONE("one"),
        ZERO("zero"),
        ;

        fun getByValue(value: String): Easing? {
           return Easing.values().find { this.value == value }
        }
    }
}

