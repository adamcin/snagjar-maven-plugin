package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.{Parameter, Mojo}
import java.io.File
import org.apache.maven.model.{Dependency, DependencyManagement, Model}
import collection.{mutable, JavaConversions}
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import scalax.io.Resource
import collection.immutable.TreeSet

/**
 * Snags artifacts into a sorted, distincted dependencyManagement block in a stub maven pom file
 * @version $Id: SnagToDepsMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-deps", requiresProject = false)
class SnagToDepsMojo extends AbstractSnagJarMojo {

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------

  /**
   * Write resulting dependencyManagement section to this path
   */
  @Parameter(property = "depsFile", defaultValue = "deps.xml")
  var depsFile: File = null

  /**
   * Set the 'scope' element for all the snagged dependencies to this value
   */
  @Parameter(property = "scope")
  var scope: String = null

  // -----------------------------------------------
  // Members
  // -----------------------------------------------

  var gavs = TreeSet.empty[GAV]

  override def begin() {
    super.begin()
    if (depsFile.exists()) { depsFile.delete() }
  }

  def snagArtifact(artifact: Snaggable) {
    gavs += artifact.gav
  }

  override def end() {
    super.end()

    val model = new Model
    val dm = new DependencyManagement
    val modelWriter = new MavenXpp3Writer

    model.setDependencyManagement(dm)

    gavs foreach { gav => dm.addDependency(gavToDep(gav)) }

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

  override def printParams() {
    super.printParams()
    getLog.info("depsFile: " + depsFile)
    getLog.info("scope: " + scope)
  }

}