package net.adamcin.maven.snagjar

import org.apache.maven.plugins.annotations.{Parameter, Mojo}
import java.io.File
import org.apache.maven.model.{Dependency, DependencyManagement, Model}
import collection.JavaConversions
import org.apache.maven.model.io.xpp3.MavenXpp3Writer
import scalax.io.Resource

/**
 *
 * @version $Id: SnagToDepsMojo.java$
 * @author madamcin
 */
@Mojo(name = "to-deps", requiresProject = false)
class SnagToDepsMojo extends AbstractSnagJarMojo {

  // -----------------------------------------------
  // Maven Parameters
  // -----------------------------------------------
  @Parameter(property = "depsFile", defaultValue = "deps.xml")
  var depsFile: File = null

  @Parameter(property = "scope")
  var scope: String = null

  // -----------------------------------------------
  // Members
  // -----------------------------------------------
  val model = new Model
  val dm = new DependencyManagement
  val modelWriter = new MavenXpp3Writer

  override def begin() {
    super.begin()
    model.setDependencyManagement(dm)
    if (depsFile.exists()) { depsFile.delete() }
  }

  def snagArtifact(artifact: Snaggable) {
    val dep = new Dependency
    dep.setGroupId(artifact.gav.groupId)
    dep.setArtifactId(artifact.gav.artifactId)
    dep.setVersion(artifact.gav.version)
    dep.setScope(scope)
    dm.addDependency(dep)
  }

  override def end() {
    super.end()

    val sorted = JavaConversions.collectionAsScalaIterable(dm.getDependencies).toList.sortWith(depLtDep)
    dm.setDependencies(JavaConversions.seqAsJavaList(sorted))

    getLog.info("Writing " + dm.getDependencies.size + " snagged dependencies to " + depsFile.getPath)

    Resource.fromFile(depsFile).outputStream.acquireAndGet(modelWriter.write(_, model))
  }

  def depLtDep(left: Dependency, right: Dependency): Boolean =
    left.getGroupId < right.getGroupId || left.getArtifactId < right.getArtifactId ||
      left.getVersion < right.getVersion


  override def printParams() {
    super.printParams()
    getLog.info("depsFile: " + depsFile)
    getLog.info("scope: " + scope)
  }

}