package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.repository.{ArtifactTransferListener, ArtifactTransferEvent}
import collection.immutable.TreeSet

class ToRemoteContext(val deployedGAVs: TreeSet[GAV])

/**
 * @version $Id: SnagToRemoteMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-remote", requiresProject = false)
class SnagToRemoteMojo extends AbstractSnagJarMojo[TreeSet[GAV]] with DeploysToRemoteRepository {

  val listener = new ArtifactTransferListener {
    def transferCompleted(p1: ArtifactTransferEvent) {
      getLog.info("[to-remote] transfer completed: " + p1.getResource)
    }

    def transferInitiated(p1: ArtifactTransferEvent) {
      getLog.debug("[to-remote] transfer initiated: " + p1)
    }

    def isShowChecksumEvents = false

    def transferProgress(p1: ArtifactTransferEvent) { }

    def setShowChecksumEvents(p1: Boolean) {}

    def transferStarted(p1: ArtifactTransferEvent) {
      getLog.debug("[to-remote] transfer started: " + p1.getResource)
    }
  }

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  // override this method to perform some setup logic
  def begin() = TreeSet.empty[GAV]

  // override this method to perform logic on each snagged artifact
  def snagArtifact(context: TreeSet[GAV], artifact: Snaggable) = {

    if (context.contains(artifact.gav)) {
      getLog.info("[to-remote] artifact already deployed: " + artifact.gav.toString)
      context
    } else {
      getLog.info(artifact.gav.toString)

      deploy(artifact, listener)

      context + artifact.gav
    }
  }

  // override this method to perform logic after all artifacts have been snagged
  def end(context: TreeSet[GAV]) {}
}