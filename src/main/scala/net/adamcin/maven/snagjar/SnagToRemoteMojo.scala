package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.repository.{ArtifactTransferListener, ArtifactTransferEvent}
import collection.immutable.TreeSet

class ToRemoteContext(val deployedGAVs: TreeSet[GAV])

/**
 * TODO Implement
 * @version $Id: SnagToRemoteMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-remote", requiresProject = false)
class SnagToRemoteMojo extends AbstractSnagJarMojo with AccessToRepositories {

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


  type SnagContext = ToRemoteContext

  // override this method to perform some setup logic
  def begin() = new ToRemoteContext(TreeSet.empty[GAV])

  // override this method to perform logic on each snagged artifact
  def snagArtifact(context: SnagContext, artifact: Snaggable) = {

    if (context.deployedGAVs.contains(artifact.gav)) {
      getLog.info("[to-remote] artifact already deployed: " + artifact.gav.toString)
      context
    } else {
      getLog.info(artifact.gav.toString)

      val (m2artifact, m2meta) = snaggableToArtifact(artifact)

      repositorySystem.publish(
        remoteRepository,
        artifact.jar,
        remoteRepository.getLayout.pathOf(m2artifact),
        listener)

      repositorySystem.publish(
        remoteRepository,
        artifact.pom,
        remoteRepository.getLayout.pathOfRemoteRepositoryMetadata(m2meta),
        listener)

      new ToRemoteContext(context.deployedGAVs + artifact.gav)
    }
  }

  // override this method to perform logic after all artifacts have been snagged
  def end(context: SnagContext) {}
}