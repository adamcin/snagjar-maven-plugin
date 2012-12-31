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

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File
import net.adamcin.snagjar.{SnagSession, Snaggable}

/**
 * Base snagjar mojo defining common parameters and basic begin-iterate-end logic
 * @since 0.8.0
 * @author Mark Adamcin
 */
abstract class AbstractSnagJarMojo[SnagContext] extends AbstractMojo {
  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------
  final val defaultFilter = "*"
  final val defaultIndexFile = "snagIndex.txt"
  final val defaultSnagFile = "."

  /**
   * applies a standard GAV (groupId:artifactId:version) filter to the snagged artifacts
   * (format: *:*:*)
   */
  @Parameter(property = "filter", defaultValue = defaultFilter)
  val filter = defaultFilter

  /**
   * specify the location of the generated index file, useful for subsequent
   * shell processing of snagged artifacts
   */
  @Parameter(property = "indexFile", defaultValue = defaultIndexFile)
  val indexFile = new File(defaultIndexFile)

  /**
   * jar or directory containing jars to snag
   */
  @Parameter(property = "snagFile", defaultValue = defaultSnagFile)
  val snagFile = new File(defaultSnagFile)

  /**
   * set to true to skip mojo execution altogether
   */
  @Parameter(property = "skip")
  val skip = false

  /**
   * set to true to recursively scan directories for jar files
   */
  @Parameter(property = "recursive")
  val recursive = false

  // -----------------------------------------------
  // Methods to Override
  // -----------------------------------------------

  // override this method to perform some setup logic
  def begin(): SnagContext

  // override this method to perform logic on each snagged artifact
  def snagArtifact(context: SnagContext, artifact: Snaggable): SnagContext

  // override this method to perform logic after all artifacts have been snagged
  def end(context: SnagContext)

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  /**
   * core mojo method. do not override.
   */
  final def execute() {
    if (skip) {
      // skip mojo execution if configured to do so
      getLog.info("Skipping...")

    } else {
      // create the session
      val session = new SnagSession(filter, indexFile, snagFile, recursive)

      try {
        // 1. foldLeft begins with begin(),
        // 2. iterates over all the snaggable artifacts and call the snagArtifact implementation on each
        // 3. end() is called on last returned context
        // ...
        end( session.findArtifacts.foldLeft (begin()) { snagArtifact } )

      } finally {
        // close the session to clean up all temporary filesystem resources
        session.close()
      }
    }
  }
}