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

import org.apache.maven.plugins.annotations.{Mojo, Parameter}
import java.io.File

import org.apache.maven.model.{Dependency, DependencyManagement, Model}
import org.apache.maven.model.io.xpp3.MavenXpp3Writer

import scalax.io.Resource
import collection.immutable.TreeSet
import org.apache.maven.plugin.MojoExecutionException
import net.adamcin.snagjar.{AccessToRepositories, GAV, Snaggable}

/**
 * Snags artifacts into a sorted, unique &lt;dependencyManagement&gt; block in a stub maven pom file
 * @since 0.8.0
 * @author Mark Adamcin
 */
@Mojo(name = "dependencies", requiresProject = false)
class DependenciesMojo extends AbstractSnagJarMojo[TreeSet[GAV]] with AccessToRepositories {

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------
  final val defaultDepsFile = "deps.xml"

  /**
   * Write resulting dependencyManagement section to this path
   */
  @Parameter(property = "depsFile", defaultValue = defaultDepsFile)
  val depsFile: File = new File(defaultDepsFile)

  /**
   * Set the 'scope' element for all the snagged dependencies to this value
   */
  @Parameter(property = "scope")
  val scope: String = null

  /**
   * In the case where multiple versions of an artifact are snagged, specify
   * "high" to include only the highest version in the dependencies, "low" to
   * include only the lowest version in the dependencies, or "none" to include
   * all versions in the dependencies, which would require manual correction
   * to remove duplicates before use in a project dependency tree
   */
  @Parameter(property = "mergeVersions", defaultValue = "high")
  val mergeVersions: String = "high"


  // -----------------------------------------------
  // Members
  // -----------------------------------------------
  def begin() = {
    if (depsFile.exists()) { depsFile.delete() }
    TreeSet.empty[GAV]
  }

  def snagArtifact(context: TreeSet[GAV], artifact: Snaggable) = {
    if (isResolvable(artifact)) {
      context + artifact.gav
    } else if (generatePoms) {
      val generated = artifact.toGenerated()
      getLog.info(s"Generating pom for artifact: ${artifact.gav} -> ${generated.gav}")
      context + generated.gav
    } else {
      getLog.info(s"Skipping artifact with unresolvable parent pom: ${artifact.gav} -> ${artifact.gav.parent}")
      context
    }
  }

  def end(context: TreeSet[GAV]) {
    val model = new Model
    val dm = new DependencyManagement
    val modelWriter = new MavenXpp3Writer

    model.setDependencyManagement(dm)

    mergeGavs(context) foreach { gav => dm.addDependency(gavToDep(gav)) }

    getLog.info("Writing " + dm.getDependencies.size + " snagged dependencies to " + depsFile.getPath)

    Resource.fromFile(depsFile).outputStream.acquireAndGet(modelWriter.write(_, model))
  }

  def mergeGavs(gavs: TreeSet[GAV]): List[GAV] = {

    def merge(mergeOp: (GAV, GAV) => GAV): List[GAV] = gavs.foldLeft(List.empty[GAV]) {
      (list, gav) => list match {
        case Nil => List(gav)
        case lastGav :: otherGavs => {
          if (gav.compareNoVersion(lastGav) != 0) {
            gav :: list
          } else {
            mergeOp(gav, lastGav) :: otherGavs
          }
        }
      }
    }

    mergeVersions match {
      case "none" => gavs.toList
      case "high" => merge(_ max _).reverse
      case "low" => merge(_ min _).reverse
      case _ => throw new MojoExecutionException("Invalid mergeVersions value. Please specify 'high', 'low', or 'none'.")
    }
  }

  def gavToDep(gav: GAV): Dependency = {
    val dep = new Dependency
    dep.setGroupId(gav.groupId)
    dep.setArtifactId(gav.artifactId)
    dep.setVersion(gav.version)
    dep.setType("jar")
    dep.setScope(scope)
    dep
  }

}