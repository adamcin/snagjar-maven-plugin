/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 *
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 *
 * For more information, please refer to <http://unlicense.org/>
 */

package net.adamcin.snagjar

import java.io.File

import org.apache.maven.artifact.repository.{ArtifactRepository, ArtifactRepositoryPolicy, DefaultRepositoryRequest, RepositoryRequest}
import org.apache.maven.plugins.annotations.{Component, Parameter}
import org.apache.maven.repository.RepositorySystem
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import org.apache.maven.settings.Settings

import scala.Option
import org.apache.maven.project.artifact.ProjectArtifactMetadata
import org.apache.maven.artifact.metadata.ArtifactMetadata
import org.apache.maven.artifact.Artifact
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest
import org.apache.maven.execution.MavenSession

/**
 * Trait defining common mojo parameters and methods useful for accessing maven repositories
 * @since 0.8.0
 * @author Mark Adamcin
 */
trait AccessToRepositories {

  // -----------------------------------------------
  // Injected Maven Components
  // -----------------------------------------------
  @Parameter(defaultValue = "${settings}", readonly = true)
  var settings: Settings = null

  @Parameter(defaultValue = "${session}", readonly = true)
  var session: MavenSession = null

  @Component
  var repositorySystem: RepositorySystem = null

  @Component(role = classOf[ArtifactRepositoryLayout])
  var repositoryLayouts: java.util.Map[String, ArtifactRepositoryLayout] = null

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------

  /**
   * Specify the repository layout to use for local and remote repositories
   */
  @Parameter(property = "repositoryLayout")
  val repositoryLayout: String = null

  /**
   * Specify true to install generated "jar" pom when a parent pom is unresolvable,
   * which omits dependencies and the parent pom reference
   * @since 1.2.0
   */
  @Parameter(property = "generatePoms")
  val generatePoms: Boolean = false

  /**
   * Specify the local repository path
   * Refer to maven-install-plugin:install-file
   */
  @Parameter(property = "localRepositoryPath")
  val localRepositoryPath: File = null

  lazy val localRepository: ArtifactRepository =
    Option(localRepositoryPath) match {
      case Some(path) => repositorySystem.createLocalRepository(path)
      case None => repositorySystem.createDefaultLocalRepository()
    }

  lazy val repositoryRequest: RepositoryRequest = {
    val request = DefaultRepositoryRequest.getRepositoryRequest(session, null)
    request.setLocalRepository(localRepository)
    request
  }

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

  def snaggableToArtifact(s: Snaggable): (Artifact, ProjectArtifactMetadata) = {
    val a = repositorySystem.createArtifact(s.gav.groupId, s.gav.artifactId, s.gav.version, "jar")
    a.setFile(s.jar)
    (a, new ProjectArtifactMetadata(a, s.pom))
  }

  def isResolvable(s: Snaggable): Boolean = {
    s.gav.parent.forall { parentGav =>
      val parentArtifact = repositorySystem.createProjectArtifact(
        parentGav.groupId, parentGav.artifactId, parentGav.version)
      val request = new ArtifactResolutionRequest(repositoryRequest)
      request.setArtifact(parentArtifact)
      repositorySystem.resolve(request).isSuccess
    }
  }
}