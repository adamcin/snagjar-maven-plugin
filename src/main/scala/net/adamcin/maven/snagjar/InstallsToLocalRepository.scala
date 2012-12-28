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

import java.io.File
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.repository.ArtifactTransferListener

/**
 * Trait defining common mojo parameters and methods necessary for installation
 * of maven artifacts to local repositories
 * @since 0.8.0
 * @author Mark Adamcin
 */
trait InstallsToLocalRepository extends AccessToRepositories {

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
}