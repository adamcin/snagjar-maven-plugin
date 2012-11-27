package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.repository.{ArtifactTransferEvent, ArtifactTransferListener}

class ToLocalContext(val installedGAVs: Set[GAV])

/**
 * @version $Id: SnagToLocalMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-local", requiresProject = false)
class SnagToLocalMojo extends AbstractSnagJarMojo[ToLocalContext] with InstallsToLocalRepository {

  val listener = new ArtifactTransferListener {
    def transferCompleted(p1: ArtifactTransferEvent) {
      getLog.info("[to-local] transfer completed: " + p1.getResource)
    }

    def transferInitiated(p1: ArtifactTransferEvent) {
      getLog.debug("[to-local] transfer initiated: " + p1)
    }

    def isShowChecksumEvents = false

    def transferProgress(p1: ArtifactTransferEvent) { }

    def setShowChecksumEvents(p1: Boolean) {}

    def transferStarted(p1: ArtifactTransferEvent) {
      getLog.debug("[to-local] transfer started: " + p1.getResource)
    }
  }

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  // override this method to perform some setup logic
  def begin() = new ToLocalContext(Set.empty[GAV])

  // override this method to perform logic on each snagged artifact
  def snagArtifact(context: ToLocalContext, artifact: Snaggable) = {
    if (context.installedGAVs.contains(artifact.gav)) {
      getLog.info("[to-local] artifact already installed: " + artifact.gav.toString)
      context
    } else {
      getLog.info(artifact.gav.toString)

      install(artifact, listener)

      new ToLocalContext(context.installedGAVs + artifact.gav)
    }
  }

  // override this method to perform logic after all artifacts have been snagged
  def end(context: ToLocalContext) { }


  override def printParams() {
    super.printParams()
  }
}