package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import java.io.File

/**
 *
 * @version $Id: SnagToLogMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-log", requiresProject = false)
class SnagToLogMojo extends AbstractSnagJarMojo {

  var artifactCount = 0

  override def begin() {
    super.begin()
    getLog.info("------------------------------------------------------------------------")
    getLog.info("Snagging Artifacts to Log...")
    getLog.info("------------------------------------------------------------------------")
  }

  def snagArtifact(artifact: Snaggable) {
    getLog.info(artifact.gav.toString)
    getLog.info("\t\t=> " + toRelative(artifact.session.snagFile, artifact.jar.getAbsolutePath))
    getLog.info("")
    artifactCount += 1
  }

  override def end() {
    super.end()
    getLog.info("------------------------------------------------------------------------")
    getLog.info("# Artifacts: " + artifactCount)
  }

  override def printParams() {
    super.printParams()
  }

  def toRelative(basedir: File, absolutePath: String) = {
    val rightSlashPath = absolutePath.replace('\\', '/');
    val basedirPath = basedir.getAbsolutePath().replace('\\', '/');
    if (rightSlashPath.startsWith(basedirPath)) {
      val fromBasePath = rightSlashPath.substring(basedirPath.length);
      val noSlash =
        if (fromBasePath.startsWith("/")) {
          fromBasePath.substring(1);
        } else {
          fromBasePath
        }

      if (noSlash.length() <= 0) {
        "."
      } else {
        noSlash
      }

    } else {
      rightSlashPath;
    }
  }
}