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

  val jarDir = new File("target/test-classes")
  val fooJar = new File("target/test-classes/foo/bundle-foo-1.0.jar")
  val barJar = new File("target/test-classes/bar/bundle-bar-1.0.jar")
  val foobarJar = new File("target/test-classes/bundle-foobar-1.0.jar")

  test("snag jar with no dependencies") {
    val indexFile = new File("target/snagIndex.txt")
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, fooJar, true)
    assert(!session.findArtifacts.isEmpty)
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 1)
    assert(lines(0) === "foo:bundle-foo:1.0")
    session.close()
  }
}