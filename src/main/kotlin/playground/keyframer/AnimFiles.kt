package playground.keyframer

import java.io.File

enum class ResFiles(var filename: String) {
    BASIC_ANIM("demo-envelope-01.json"),
    REPEATED_ANIM("repeatedAnim.json"),
    ;

    val path = listOf(
        System.getProperty("user.dir"), "src", "main", "resources", filename
    ).joinToString(File.pathSeparator)
}
