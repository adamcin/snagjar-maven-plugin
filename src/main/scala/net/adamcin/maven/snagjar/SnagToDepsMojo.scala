package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.{Parameter, Mojo}
import java.io.File
import org.apache.maven.model.{Dependency, DependencyManagement, Model}
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import scalax.io.Resource
import collection.immutable.TreeSet
import org.apache.maven.plugin.logging.Log

/**
 * Snags artifacts into a sorted, distincted dependencyManagement block in a stub maven pom file
 * @version $Id: SnagToDepsMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-deps", requiresProject = false)
class SnagToDepsMojo extends AbstractSnagJarMojo[TreeSet[GAV]] {

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

  // -----------------------------------------------
  // Members
  // -----------------------------------------------
  def begin() = {
    if (depsFile.exists()) { depsFile.delete() }
    TreeSet.empty[GAV]
  }

  def snagArtifact(context: TreeSet[GAV], artifact: Snaggable) = {
    context + artifact.gav
  }

  def end(context: TreeSet[GAV]) {
    val model = new Model
    val dm = new DependencyManagement
    val modelWriter = new MavenXpp3Writer

    model.setDependencyManagement(dm)

    context foreach { gav => dm.addDependency(gavToDep(gav)) }

    getLog.info("Writing " + dm.getDependencies.size + " snagged dependencies to " + depsFile.getPath)

    Resource.fromFile(depsFile).outputStream.acquireAndGet(modelWriter.write(_, model))
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

  override def printParams(log: Log) {
    super.printParams(log)
    log.info("depsFile: " + depsFile)
    log.info("scope: " + scope)
  }

}