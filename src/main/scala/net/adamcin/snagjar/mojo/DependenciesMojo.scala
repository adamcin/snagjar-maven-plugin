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

import org.apache.maven.model._
import org.apache.maven.model.io.xpp3.MavenXpp3Writer

import scalax.io.Resource
import collection.immutable.TreeSet
import org.apache.maven.plugin.MojoExecutionException
import net.adamcin.snagjar.{AccessToRepositories, GAV, Snaggable}
import org.apache.maven.artifact.repository.{ArtifactRepository, ArtifactRepositoryPolicy}

object DependenciesMojo {
  final val PROP_SCOPE = "scope"
  final val PROP_MERGE_VERSIONS = "mergeVersions"
  final val MOJO_NAME = "dependencies"
}

/**
 * Snags artifacts into a sorted, unique &lt;dependencyManagement&gt; block in a stub maven pom file
 * @since 0.8.0
 * @author Mark Adamcin
 */
@Mojo(name = DependenciesMojo.MOJO_NAME, requiresProject = false)
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
  @Parameter(property = DependenciesMojo.PROP_SCOPE)
  val scope: String = null

  /**
   * In the case where multiple versions of an artifact are snagged, specify
   * "high" to include only the highest version in the dependencies, "low" to
   * include only the lowest version in the dependencies, or "none" to include
   * all versions in the dependencies, which would require manual correction
   * to remove duplicates before use in a project dependency tree
   */
  @Parameter(property = DependenciesMojo.PROP_MERGE_VERSIONS, defaultValue = "high")
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

  def convertSettingsRepo(it: org.apache.maven.settings.Repository): Repository = {
    def convertPolicy(otherPolicy: org.apache.maven.settings.RepositoryPolicy): RepositoryPolicy = {
      val policy = new RepositoryPolicy()
      policy.setEnabled(otherPolicy.isEnabled)
      policy.setUpdatePolicy(otherPolicy.getUpdatePolicy)
      policy.setChecksumPolicy(otherPolicy.getChecksumPolicy)
      policy
    }

    val repo = new Repository()
    repo.setId(it.getId)
    repo.setLayout(it.getLayout)
    repo.setUrl(it.getUrl)
    repo.setName(it.getName)
    repo.setReleases(convertPolicy(it.getReleases))
    repo.setSnapshots(convertPolicy(it.getSnapshots))
    repo
  }

  def convertArtifactRepo(it: ArtifactRepository): Repository = {
    def convertPolicy(otherPolicy: ArtifactRepositoryPolicy): RepositoryPolicy = {
      val policy = new RepositoryPolicy()
      policy.setEnabled(otherPolicy.isEnabled)
      policy.setUpdatePolicy(otherPolicy.getUpdatePolicy)
      policy.setChecksumPolicy(otherPolicy.getChecksumPolicy)
      policy
    }

    val repo = new Repository()
    repo.setId(it.getId)
    repo.setLayout(it.getLayout.getId)
    repo.setUrl(it.getUrl)
    repo.setReleases(convertPolicy(it.getReleases))
    repo.setSnapshots(convertPolicy(it.getSnapshots))
    repo
  }

  def end(context: TreeSet[GAV]) {
    val model = new Model
    val dm = new DependencyManagement
    val modelWriter = new MavenXpp3Writer

    model.setModelVersion("4.0.0")
    model.setPackaging("pom")
    model.setDescription(buildDescription())

    model.setDependencyManagement(dm)

    if (reposFromSettings.nonEmpty) {
      import scala.collection.JavaConverters._
      val repos = reposFromSettings.map(convertSettingsRepo)
      model.setRepositories(repos.asJava)
    }

    mergeGavs(context) foreach { gav => dm.addDependency(gavToDep(gav)) }

    getLog.info("Writing " + dm.getDependencies.size + " snagged dependencies to " + depsFile.getPath)

    Resource.fromFile(depsFile).outputStream.acquireAndGet(modelWriter.write(_, model))
  }

  def buildDescription(): String = {
    import scala.collection.JavaConverters._
    import DependenciesMojo._
    import AbstractSnagJarMojo.PROP_SNAG_FILE
    import AbstractSnagJarMojo.PROP_RECURSIVE
    import AccessToRepositories.PROP_GENERATE_POMS

    val groupId = pluginProps("groupId")
    val artifactId = pluginProps("artifactId")
    val version = pluginProps("version")

    val profiles = if (!settings.getActiveProfiles.isEmpty) {
      s"-P '${settings.getActiveProfiles.asScala.mkString(",")}'"
    } else {
      ""
    }

    val baseCommand = s"mvn $groupId:$artifactId:$version:$MOJO_NAME $profiles"

    val dRecursive = if (recursive) Some(s"$PROP_RECURSIVE") else None
    val dScope = Option(scope).map(it => s"$PROP_SCOPE=$it")
    val dGeneratePoms = if (generatePoms) Some(s"$PROP_GENERATE_POMS") else None
    val dMergeVersions = Some(s"$PROP_MERGE_VERSIONS=$mergeVersions")
    val dSnagFile = Some(s"$PROP_SNAG_FILE=${snagFile.getAbsolutePath}")
    val dList = List(dRecursive, dScope, dGeneratePoms, dMergeVersions, dSnagFile).flatten
      .map(it => s"-D$it").mkString(" ")
    s"[regen cmd] $baseCommand $dList".replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
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