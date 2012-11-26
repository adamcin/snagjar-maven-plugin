package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo

class ToRemoteContext

/**
 * TODO Implement
 * @version $Id: SnagToRemoteMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-remote", requiresProject = false)
class SnagToRemoteMojo extends AbstractSnagJarMojo with AccessToRepositories {
  type SnagContext = ToRemoteContext

  // override this method to perform some setup logic
  def begin() = new ToRemoteContext

  // override this method to perform logic on each snagged artifact
  def snagArtifact(context: SnagContext, artifact: Snaggable) = context

  // override this method to perform logic after all artifacts have been snagged
  def end(context: SnagContext) {}
}