package net.adamcin.maven.snagjar

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import org.slf4j.{Logger, LoggerFactory}
import scalax.io.Resource

/**
 *
 * @version $Id: SnaggableTest.java$
 * @author madamcin
 */
@RunWith(classOf[JUnitRunner])
class SnaggableTest extends FunSuite {
  val log: Logger = LoggerFactory.getLogger(getClass)

  val testJar = new File("target/test-classes/scala-compiler-bundle.jar")

  test("create SnagSession") {
    val indexFile = new File("target/snagIndex.txt")
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, testJar, true)
    assert(!session.findArtifacts.isEmpty)
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 1)
    assert(lines(0).startsWith("org.scala-lang:scala-compiler-bundle:"), "line matches groupId:artifactId")
    session.close()
  }
}