package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo

/**
 * TODO Implement
 * @version $Id: SnagToLocalMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-local", requiresProject = false)
class SnagToLocalMojo extends AbstractSnagJarMojo with AccessToRepositories {


  /*
  def snagArtifact(context: SnagContext, artifact: Snaggable) {
    getLog.info(artifact.gav.toString)
  }
  */

  override def printParams() {
    super.printParams()
  }
}