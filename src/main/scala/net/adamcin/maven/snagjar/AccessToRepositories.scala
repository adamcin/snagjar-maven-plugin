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