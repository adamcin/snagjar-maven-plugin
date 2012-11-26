package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo

class ToLocalContext

/**
 * TODO Implement
 * @version $Id: SnagToLocalMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-local", requiresProject = false)
class SnagToLocalMojo extends AbstractSnagJarMojo with AccessToRepositories {
  type SnagContext = ToLocalContext

  /*
  def snagArtifact(context: SnagContext, artifact: Snaggable) {
    getLog.info(artifact.gav.toString)
  }
  */

  // override this method to perform some setup logic
  def begin() = new ToLocalContext

  // override this method to perform logic on each snagged artifact
  def snagArtifact(context: ToLocalContext, artifact: Snaggable) = context

  // override this method to perform logic after all artifacts have been snagged
  def end(context: ToLocalContext) { }

  override def printParams() {
    super.printParams()
  }
}