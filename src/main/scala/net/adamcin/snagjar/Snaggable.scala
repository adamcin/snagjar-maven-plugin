/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */

package net.adamcin.snagjar

import java.io.File

/**
 * Simple class representing a discovered jar, its maven coordinates, and its pom file
 * @param session the current SnagSession
 * @param gav the artifact's maven coordinates
 * @param jar the artifact's jar file
 * @param pom the artifact's pom file
 * @since 0.8.0
 * @author Mark Adamcin
 */
class Snaggable(val session: SnagSession,
                val gav: GAV,
                val jar: File,
                val pom: File) {

}

/**
 * Companion class defining a convenient apply method
 */
object Snaggable {

  /**
   * Extract the provided file with the provided SnagSession
   * @param file jar file to extract
   * @param session SnagSession to extract the file with
   * @return an extracted Snaggable
   */
  def apply(file: File, session: SnagSession): Snaggable = SnagSession.extract(file, session)
}