package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo

/**
 *
 * @version $Id: SnagToLocalMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-local", requiresProject = false)
class SnagToLocalMojo extends AbstractSnagJarMojo with AccessToRepositories {


  def snagArtifact(jar: Snaggable) {
    getLog.info(jar.gav.toString)
  }

  override def printParams() {
    super.printParams()
  }
}