package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo

/**
 * TODO Implement
 * @version $Id: SnagToRemoteMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-remote", requiresProject = false)
class SnagToRemoteMojo extends AbstractSnagJarMojo with AccessToRepositories {

  // override this method to perform logic on each snagged artifact
  def snagArtifact(artifact: Snaggable) {

  }
}