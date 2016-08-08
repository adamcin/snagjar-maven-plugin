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

import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
import org.apache.maven.plugins.annotations.{Parameter, Component}
import org.apache.maven.repository.RepositorySystem
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
import org.apache.maven.settings.Settings
import scala.Option
import org.apache.maven.project.artifact.ProjectArtifactMetadata
import org.apache.maven.artifact.metadata.ArtifactMetadata
import org.apache.maven.artifact.Artifact

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
   * Specify true to install generated "jar" poms, which omit dependencies and
   * parent pom references
   * @since 0.8.2
   */
  @Parameter(property = "generatePoms")
  val generatePoms: Boolean = false

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
    (a, new ProjectArtifactMetadata(a, if (generatePoms) s.genPom else s.pom))
  }
}