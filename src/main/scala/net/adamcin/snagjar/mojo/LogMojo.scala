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

package net.adamcin.snagjar.mojo

import org.apache.maven.plugins.annotations.Mojo
import java.io.File
import net.adamcin.snagjar.Snaggable

/**
 * Mojo that writes all snagged artifacts to the maven execution log
 * @since 0.8.0
 * @author Mark Adamcin
 */
@Mojo(name = "log", requiresProject = false)
class LogMojo extends AbstractSnagJarMojo[Int] {

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  def begin() = {
    getLog.info("------------------------------------------------------------------------")
    getLog.info("Snagging Artifacts to Log...")
    getLog.info("------------------------------------------------------------------------")
    0
  }

  def snagArtifact(context: Int, artifact: Snaggable) = {
    getLog.info(artifact.gav.toString)
    getLog.info("\t\t=> " + toRelative(artifact.session.snagFile, artifact.jar.getAbsolutePath))
    getLog.info("")
    context + 1
  }

  def end(context: Int) {
    getLog.info("------------------------------------------------------------------------")
    getLog.info("# Artifacts: " + context)
  }

  def toRelative(basedir: File, absolutePath: String) = {
    val rightSlashPath = absolutePath.replace('\\', '/')
    val basedirPath = basedir.getAbsolutePath.replace('\\', '/')
    if (rightSlashPath.startsWith(basedirPath)) {
      val fromBasePath = rightSlashPath.substring(basedirPath.length)
      val noSlash =
        if (fromBasePath.startsWith("/")) {
          fromBasePath.substring(1)
        } else {
          fromBasePath
        }

      if (noSlash.length() <= 0) {
        "."
      } else {
        noSlash
      }

    } else {
      rightSlashPath
    }
  }
}