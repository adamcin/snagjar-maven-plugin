package net.adamcin.maven.snagjar

import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import collection.JavaConversions
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.repository.ArtifactTransferListener

/**
 *
 * @version $Id: DeploysToRemoteRepository.java$
 * @author madamcin
 */
trait DeploysToRemoteRepository extends AccessToRepositories {

  @Parameter(property = "url")
  val url: String = null

  @Parameter(property = "repositoryId")
  val repositoryId: String = null

  lazy val remoteRepository: ArtifactRepository =
    (Option(repositoryId), Option(url)) match {
      case (Some(pId), Some(pUrl)) =>
        repositorySystem.createArtifactRepository(pId, pUrl, layout, snapshotPolicy, releasePolicy)
      case (None, Some(pUrl)) =>
        repositorySystem.createArtifactRepository(null, pUrl, layout, snapshotPolicy, releasePolicy)
      case (_, _) =>
        repositorySystem.createDefaultRemoteRepository()
    }

  def deploy(artifact: Snaggable, listener: ArtifactTransferListener ) {
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
  }
  override def printParams() {
    super.printParams()

    getLog.info("url: " + url)
    getLog.info("repositoryId: " + repositoryId)

    val remoteRepoOption = Option(remoteRepository)
    getLog.info("remoteRepository is empty? " + remoteRepoOption.isEmpty)
    remoteRepoOption match {
      case Some(repo) =>
        getLog.info("remoteRepository id: " + repo.getId)
        getLog.info("remoteRepository url: " + repo.getUrl)
      case None =>
    }
  }
}
