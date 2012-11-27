package net.adamcin.maven.snagjar

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 * Base snagjar mojo defining common parameters and basic begin-iterate-end logic
 * TODO Find or implement some way to enable scaladoc-as-maven-help
 * @version $Id: AbstractSnagJarMojo.java$
 * @author madamcin
 */
abstract class AbstractSnagJarMojo extends AbstractMojo {
  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------
  final val defaultFilter = "*"
  final val defaultIndexFile = "snagIndex.txt"
  final val defaultSnagFile = "."

  /**
   * applies a standard GAV (groupId:artifactId:version) filter to the snagged artifacts
   * (format: *:*:*)
   * @since 1.0
   */
  @Parameter(property = "filter", defaultValue = defaultFilter)
  val filter = defaultFilter

  /**
   * specify the location of the generated index file, useful for subsequent
   * shell processing of snagged artifacts
   * @since 1.0
   */
  @Parameter(property = "indexFile", defaultValue = defaultIndexFile)
  val indexFile = new File(defaultIndexFile)

  /**
   * jar or directory containing jars to snag
   * @since 1.0
   */
  @Parameter(property = "snagFile", defaultValue = defaultSnagFile)
  val snagFile = new File(defaultSnagFile)

  /**
   * set to true to skip mojo execution altogether
   * @since 1.0
   */
  @Parameter(property = "skip")
  val skip = false

  /**
   * set to true to recursively scan directories for jar files
   * @since 1.0
   */
  @Parameter(property = "recursive")
  val recursive = false

  @Parameter(property = "debug")
  val debug = false

  // -----------------------------------------------
  // Methods to Override
  // -----------------------------------------------
  type SnagContext

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
    if (debug) {
      printParams()
    } else if (skip) {
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
        // This is one hell of a purely functional one-liner.
        end( session.findArtifacts.foldLeft (begin()) { snagArtifact } )

      } finally {
        // close the session to clean up all temporary filesystem resources
        session.close()
      }
    }
  }

  /**
   * print injected maven component and parameter values
   */
  def printParams() {
    getLog.info("filter: " + filter)
    getLog.info("indexFile: " + indexFile.getAbsolutePath)
    getLog.info("snagFile: " + snagFile)
    getLog.info("skip: " + skip)
    getLog.info("recursive: " + recursive)
  }
}