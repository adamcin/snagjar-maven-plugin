package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import java.io.File

class ToLogContext(val artifactCount: Int)

/**
 * Mojo that writes all snagged artifacts to the maven execution log
 * @version $Id: SnagToLogMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-log", requiresProject = false)
class SnagToLogMojo extends AbstractSnagJarMojo[ToLogContext] {

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  def begin() = {
    getLog.info("------------------------------------------------------------------------")
    getLog.info("Snagging Artifacts to Log...")
    getLog.info("------------------------------------------------------------------------")
    new ToLogContext(0)
  }

  def snagArtifact(context: ToLogContext, artifact: Snaggable) = {
    getLog.info(artifact.gav.toString)
    getLog.info("\t\t=> " + toRelative(artifact.session.snagFile, artifact.jar.getAbsolutePath))
    getLog.info("")
    new ToLogContext(context.artifactCount + 1)
  }

  def end(context: ToLogContext) {
    getLog.info("------------------------------------------------------------------------")
    getLog.info("# Artifacts: " + context.artifactCount)
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