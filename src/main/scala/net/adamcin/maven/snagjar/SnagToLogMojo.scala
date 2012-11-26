package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import java.io.File

/**
 * Mojo that writes all snagged artifacts to the maven execution log
 * @version $Id: SnagToLogMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-log", requiresProject = false)
class SnagToLogMojo extends AbstractSnagJarMojo {

  class ToLogContext(prevContext: ToLogContext) extends SnagContext {
    val artifactCount: Int = Option(prevContext) match {
      case Some(context) => context.artifactCount + 1
      case None => 0
    }
  }

  override def begin(): SnagContext = {
    super.begin()
    getLog.info("------------------------------------------------------------------------")
    getLog.info("Snagging Artifacts to Log...")
    getLog.info("------------------------------------------------------------------------")
    new ToLogContext(null)
  }

  override def snagArtifact(context: SnagContext, artifact: Snaggable): SnagContext = {
    getLog.info(artifact.gav.toString)
    getLog.info("\t\t=> " + toRelative(artifact.session.snagFile, artifact.jar.getAbsolutePath))
    getLog.info("")

    context match {
      case ctx: ToLogContext => new ToLogContext(ctx)
      case _ => context
    }
  }

  override def end(context: SnagContext) {
    super.end(context)
    context match {
      case ctx: ToLogContext => {
        getLog.info("------------------------------------------------------------------------")
        getLog.info("# Artifacts: " + context)
      }
      case _ =>
    }
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