package net.adamcin.maven.snagjar

import org.apache.maven.artifact.repository.{ArtifactRepositoryPolicy, ArtifactRepository}
import org.apache.maven.plugins.annotations.{Parameter, Component}
import org.apache.maven.repository.RepositorySystem
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import collection.JavaConversions
import java.io.File
import org.apache.maven.settings.Settings
import scala.Option
import org.apache.maven.project.artifact.ProjectArtifactMetadata
import org.apache.maven.artifact.metadata.ArtifactMetadata
import org.apache.maven.artifact.Artifact
import org.apache.maven.plugin.logging.Log

/**
 *
 * @version $Id: AccessToRepositories.java$
 * @author madamcin
 */
trait AccessToRepositories extends LogsParameters {

  // -----------------------------------------------
  // Injected Maven Components
  // -----------------------------------------------
  @Component
  var settings: Settings = null

  @Component
  var repositorySystem: RepositorySystem = null

  @Component(role = classOf[ArtifactRepositoryLayout])
  var repositoryLayouts: java.util.Map[String, ArtifactRepositoryLayout] = null

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------

  /**
   * Specify the repository layout to use for local and remote repositories
   * @since 1.0
   */
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

  def snaggableToArtifact(s: Snaggable): (Artifact, ArtifactMetadata) = {
    val a = repositorySystem.createArtifact(s.gav.groupId, s.gav.artifactId, s.gav.version, "jar")
    (a, new ProjectArtifactMetadata(a, s.pom))
  }

  override def printParams(log: Log) {
    super.printParams(log)

    log.info("settings is empty? " + Option(settings).isEmpty)
    log.info("repositorySystem is empty? " + Option(repositorySystem).isEmpty)
    log.info("repositoryLayouts is empty? " + Option(repositoryLayouts).isEmpty)

    JavaConversions.mapAsScalaMap(repositoryLayouts).foreach((p: (String, ArtifactRepositoryLayout)) => log.info("Layout " + p._1 + " casts " + classOf[ArtifactRepositoryLayout].cast(p._2)))

    log.info("repositoryLayout: " + repositoryLayout)
  }
}