package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo

/**
 *
 * @version $Id: SnagToLogMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-log", requiresProject = false)
class SnagToLogMojo extends AbstractSnagJarMojo {


  def snagArtifact(jar: Snaggable) {}

  override def printParams() {
    super.printParams()
  }
}