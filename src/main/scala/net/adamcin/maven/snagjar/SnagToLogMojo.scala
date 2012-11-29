package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import java.io.File

/**
 * Mojo that writes all snagged artifacts to the maven execution log
 * @version $Id: SnagToLogMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-log", requiresProject = false)
class SnagToLogMojo extends AbstractSnagJarMojo[Int] {

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