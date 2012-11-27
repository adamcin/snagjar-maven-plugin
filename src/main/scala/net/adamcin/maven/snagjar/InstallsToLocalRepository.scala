package net.adamcin.maven.snagjar

import java.io.File
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.repository.ArtifactTransferListener

/**
 *
 * @version $Id: InstallsToLocalRepository.java$
 * @author madamcin
 */
trait InstallsToLocalRepository extends AccessToRepositories {

  @Parameter(property = "localRepositoryPath")
  val localRepositoryPath: File = null

  lazy val localRepository: ArtifactRepository =
    Option(localRepositoryPath) match {
      case Some(path) => repositorySystem.createLocalRepository(path)
      case None => repositorySystem.createDefaultLocalRepository()
    }


  def install(artifact: Snaggable, listener: ArtifactTransferListener ) {
    val (m2artifact, m2meta) = snaggableToArtifact(artifact)

    repositorySystem.publish(
      localRepository,
      artifact.jar,
      localRepository.getLayout.pathOf(m2artifact),
      listener)

    repositorySystem.publish(
      localRepository,
      artifact.pom,
      localRepository.getLayout.pathOfLocalRepositoryMetadata(m2meta, localRepository),
      listener)
  }

  override def printParams() {
    super.printParams()

    getLog.info("localRepositoryPath: " + localRepositoryPath)

    val localRepoOption = Option(localRepository)
    getLog.info("localRepository is empty? " + localRepoOption.isEmpty)
    localRepoOption match {
      case Some(repo) =>
        getLog.info("localRepository real path: " + localRepository.getBasedir)
      case None =>
    }
  }
}