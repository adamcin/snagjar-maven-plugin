package net.adamcin.maven.snagjar

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import java.io.File

/**
 *
 * @version $Id: AbstractSnagJarMojo.java$
 * @author madamcin
 */
abstract class AbstractSnagJarMojo extends AbstractMojo {

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------
  @Parameter(property = "filter", defaultValue = "*")
  var filter: String = null

  @Parameter(property = "indexFile", defaultValue = "snagIndex.txt")
  var indexFile: File = null

  @Parameter(property = "skip")
  var skip: Boolean = false

  @Parameter(property = "recursive")
  var recursive: Boolean = false

  @Parameter(property = "snagFile", defaultValue = ".")
  var snagFile: File = null

  // -----------------------------------------------
  // Abstract Methods
  // -----------------------------------------------

  def begin() {}
  def snagArtifact(artifact: Snaggable)
  def end() {}

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  def execute() {
    if (skip) {
      getLog.info("Skipping...")
    } else {
      val session = new SnagSession(filter, indexFile, snagFile, recursive)
      try {
        begin()
        session.findArtifacts.foreach((artifact: Snaggable) => {
          snagArtifact(artifact)
        })
        end()
      } finally {
        session.close()
      }
    }
  }

  def printParams() {
    getLog.info("filter: " + filter)
    getLog.info("indexFile: " + indexFile.getAbsolutePath)
    getLog.info("skip: " + skip)
    getLog.info("recursive: " + recursive)
    getLog.info("snagFile: " + snagFile)
  }
}