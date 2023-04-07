package playground

import org.openrndr.application
import org.openrndr.color.ColorRGBa


/**
 * Prerequisite:
 * Open IntelliJ IDEA (or Android Studio).
 * Clone OPENRNDR template from GitHub as new project.
 * This file exists under <project>/src/main/kotlin.
 *
 * Voice input started here.
 * The editor was open in a new Kotlin file, only containing the java-package line.
 *
 * The editor had the text cursor was blinking at the end of file.
 *
 * Note: All statements inbetween, like imports, have been made by IntelliJ automatically.
 * Also, when I type most syntax chars like '{' , I usually think of the character itself.
 * When typing I think of it as the char '{' and not 'curly_braces' in my head.
 *
 * For the sake of this example,
 * I said out loud some syntax i usually wouldn't, like 'open' instead if '('.
 *
 * I said:
 * ```
 * function main brackets is application lambda enter
 * program lambda enter
 * drawer dot clear open black import close enter
 * drawer dot fill is white import enter
 * drawer dot circle open brackets screen_center comma 100 dot 0 close enter
 * leave
 * leave
 * leave
 * enter
 * save_file
 * ```
 *
 * This was mapped to key strokes.
 * Note: Strings are meant as multiple key strokes. Like 'fun ' = F+U+N+SPACE. Bars indicate separation of inputs.
 * ```
 * 'fun |main| = |application| {' -> code complete to ' {_}' with _ being focus -> ENTER
 * 'program| {' -> same complete to ' {_}' -> ENTER
 * 'extend| { -> same completer to ' {_} -> ENTER
 * 'drawer|.|clear|(|BLACK' -> import via ENTER -> ')' -> ENTER
 * 'drawer|.|fill|(|WHITE' -> import via ENTER -> ')' -> ENTER
 * 'drawer|.|circle|(|drawer.bounds.center|, |100|.|0|)' -> ENTER
 * KEY_DOWN (moving the text cursor down, now behind the curly brave closing the lambda)
 * KEY_DOWN (same move to just behind outer lambda) ENTER
 * KEY_DOWN (same move to just behind outer lambda) ENTER
 * CMD+S (shortcut saving the file)
 * ```
 *
 * The result is executable code. Run this in IntelliJ to start the application.
 */
fun main() = application {
    program {
        extend{
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = ColorRGBa.WHITE
            drawer.circle(drawer.bounds.center, 100.0)
        }
    }
}
