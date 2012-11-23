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

  /**
   * applies a standard GAV (groupId:artifactId:version) filter to the snagged artifacts
   * (format: *:*:*)
   * @since 1.0
   */
  @Parameter(property = "filter", defaultValue = "*")
  var filter: String = null

  /**
   * specify the location of the generated index file, useful for subsequent
   * shell processing of snagged artifacts
   * @since 1.0
   */
  @Parameter(property = "indexFile", defaultValue = "snagIndex.txt")
  var indexFile: File = null

  /**
   * jar or directory containing jars to snag
   * @since 1.0
   */
  @Parameter(property = "snagFile", defaultValue = ".")
  var snagFile: File = null

  /**
   * set to true to skip mojo execution altogether
   * @since 1.0
   */
  @Parameter(property = "skip")
  var skip = false

  /**
   * set to true to recursively scan directories for jar files
   * @since 1.0
   */
  @Parameter(property = "recursive")
  var recursive = false

  // -----------------------------------------------
  // Methods to Override
  // -----------------------------------------------

  // override this method to perform some setup logic
  def begin() {}

  // override this method to perform logic on each snagged artifact
  def snagArtifact(artifact: Snaggable)

  // override this method to perform logic after all artifacts have been snagged
  def end() {}

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
        // call the mojo's begin method
        begin()

        // iterate over all the snaggable artifacts and call the snagArtifact implementation on each
        session.findArtifacts foreach { snagArtifact }

        // call the mojo's end method
        end()

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