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

package net.adamcin.snagjar.mojo

import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.repository.{ArtifactTransferListener, ArtifactTransferEvent}
import collection.immutable.TreeSet
import net.adamcin.snagjar.{Snaggable, DeploysToRemoteRepository, GAV}

/**
 * Deploy snagged artifacts to a remote maven repository
 * @since 0.8.0
 * @author Mark Adamcin
 */
@Mojo(name = "deploy", requiresProject = false)
class DeployMojo extends AbstractSnagJarMojo[TreeSet[GAV]] with DeploysToRemoteRepository {

  val listener = new ArtifactTransferListener {
    def transferCompleted(p1: ArtifactTransferEvent) {
      getLog.info("transfer completed: " + p1.getResource)
    }

    def transferInitiated(p1: ArtifactTransferEvent) {
      getLog.debug("transfer initiated: " + p1)
    }

    def isShowChecksumEvents = false

    def transferProgress(p1: ArtifactTransferEvent) { }

    def setShowChecksumEvents(p1: Boolean) {}

    def transferStarted(p1: ArtifactTransferEvent) {
      getLog.debug("transfer started: " + p1.getResource)
    }
  }

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  def begin() = TreeSet.empty[GAV]

  def snagArtifact(context: TreeSet[GAV], artifact: Snaggable) = {
    if (isResolvable(artifact)) {
      if (context.contains(artifact.gav)) {
        getLog.info("artifact already deployed: " + artifact.gav.toString)
        context
      } else {
        getLog.info(artifact.gav.toString)
        deploy(artifact, listener)
        context + artifact.gav
      }
    } else if (generatePoms) {
      val generated = artifact.toGenerated()
      getLog.info(s"Generating pom for artifact: ${artifact.gav} -> ${generated.gav}")
      if (context.contains(generated.gav)) {
        getLog.info("artifact already deployed: " + generated.gav.toString)
        context
      } else {
        getLog.info(generated.gav.toString)
        deploy(generated, listener)
        context + generated.gav
      }
    } else {
      getLog.info(s"Skipping artifact with unresolvable parent pom: ${artifact.gav} -> ${artifact.gav.parent}")
      context
    }
  }

  def end(context: TreeSet[GAV]) {}
}