package net.adamcin.maven.snagjar

import org.apache.maven.artifact.repository.{ArtifactRepositoryPolicy, ArtifactRepository}
import org.apache.maven.plugins.annotations.{Parameter, Component}
import org.apache.maven.repository.RepositorySystem
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import collection.JavaConversions
import java.io.File
import org.apache.maven.settings.Settings
import scala.Option

/**
 *
 * @version $Id: AccessToRepositories.java$
 * @author madamcin
 */
trait AccessToRepositories extends AbstractSnagJarMojo {

  // -----------------------------------------------
  // Injected Maven Components
  // -----------------------------------------------
  @Component
  val settings: Settings = null

  @Component
  val repositorySystem: RepositorySystem = null

  @Component(role = classOf[ArtifactRepositoryLayout])
  val repositoryLayouts: java.util.Map[String, ArtifactRepositoryLayout] = null

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------
  @Parameter(property = "localRepositoryPath")
  val localRepositoryPath: File = null

  @Parameter(property = "url")
  val url: String = null

  @Parameter(property = "repositoryId")
  val repositoryId: String = null

  @Parameter(property = "repositoryLayout")
  val repositoryLayout: String = null

  // -----------------------------------------------
  // Members
  // -----------------------------------------------
  val snapshotPolicy =
    new ArtifactRepositoryPolicy(true,
      ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
      ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE)

  val releasePolicy =
    new ArtifactRepositoryPolicy(true,
      ArtifactRepositoryPolicy.UPDATE_POLICY_ALWAYS,
      ArtifactRepositoryPolicy.CHECKSUM_POLICY_IGNORE)

  // lazy evaluation occurs after dependency injection :)
  lazy val layout: ArtifactRepositoryLayout = repositoryLayouts.get(Option(repositoryLayout).getOrElse("default"))

  lazy val localRepository: ArtifactRepository =
    Option(localRepositoryPath) match {
      case Some(path) => repositorySystem.createLocalRepository(path)
      case None => repositorySystem.createDefaultLocalRepository()
    }

  lazy val remoteRepository: ArtifactRepository =
    (Option(repositoryId), Option(url)) match {
      case (Some(pId), Some(pUrl)) =>
        repositorySystem.createArtifactRepository(pId, pUrl, layout, snapshotPolicy, releasePolicy)
      case (None, Some(pUrl)) =>
        repositorySystem.createArtifactRepository(null, pUrl, layout, snapshotPolicy, releasePolicy)
      case (_, _) =>
        repositorySystem.createDefaultRemoteRepository()
    }

  override def printParams() {
    super.printParams()

    getLog.info("settings is empty? " + Option(settings).isEmpty)
    getLog.info("repositorySystem is empty? " + Option(repositorySystem).isEmpty)
    getLog.info("repositoryLayouts is empty? " + Option(repositoryLayouts).isEmpty)

    JavaConversions.mapAsScalaMap(repositoryLayouts).foreach((p: (String, ArtifactRepositoryLayout)) => getLog.info("Layout " + p._1 + " casts " + classOf[ArtifactRepositoryLayout].cast(p._2)))

    getLog.info("localRepositoryPath: " + localRepositoryPath)
    getLog.info("url: " + url)
    getLog.info("repositoryId: " + repositoryId)
    getLog.info("repositoryLayout: " + repositoryLayout)

    val localRepoOption = Option(localRepository)
    getLog.info("localRepository is empty? " + localRepoOption.isEmpty)
    localRepoOption match {
      case Some(repo) =>
        getLog.info("localRepository real path: " + localRepository.getBasedir)
      case None =>
    }

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