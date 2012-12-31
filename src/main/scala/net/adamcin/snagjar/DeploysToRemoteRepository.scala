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

import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.artifact.repository.ArtifactRepository
import org.apache.maven.repository.ArtifactTransferListener

/**
 * Trait defining common mojo parameters and methods necessary for deployment of
 * artifacts to remote repositories
 * @since 0.8.0
 * @author Mark Adamcin
 */
trait DeploysToRemoteRepository extends AccessToRepositories {

  /**
   * Specify the url of the repository to deploy to.
   */
  @Parameter(property = "url")
  val url: String = null

  /**
   * Specify the id of the server element in maven settings containing the
   * repository username and password
   */
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
}
