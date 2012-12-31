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

import org.scalatest.FunSuite
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.io.File
import org.slf4j.{Logger, LoggerFactory}
import scalax.io.Resource
import net.adamcin.snagjar.{SnagSession, Snaggable, GAV}

/**
 *
 * @version $Id: SnagSessionTest.java$
 * @author madamcin
 */
@RunWith(classOf[JUnitRunner])
class SnagSessionTest extends FunSuite {
  val log: Logger = LoggerFactory.getLogger(getClass)

  val indexFile = new File("target/snagIndex.txt")
  val jarDir = new File("target/test-classes")
  val fooJar = new File("target/test-classes/foo/bundle-foo-1.0.jar")
  val barJar = new File("target/test-classes/bar/bundle-bar-1.0.jar")
  val foobarJar = new File("target/test-classes/bundle-foobar-1.0.jar")

  test("snag jar with no dependencies") {
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, fooJar, true)
    assert(!session.findArtifacts.isEmpty)
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 1)
    assert(lines.toSet === Set("foo:bundle-foo:1.0"))
    session.close()
  }

  test("snag jar with one dependency") {
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, barJar, true)
    assert(!session.findArtifacts.isEmpty)
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 1)
    assert(lines.toSet === Set("bar:bundle-bar:1.0"))
    session.close()
  }

  test("snag jar with two embedded dependencies") {
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, foobarJar, true)
    assert(!session.findArtifacts.isEmpty)
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 1)
    assert(lines.toSet === Set("foobar:bundle-foobar:1.0"))
    session.close()
  }

  test("snag directory with foobar jar non recursive") {
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, jarDir, false)
    assert(session.findArtifacts.map((s: Snaggable) => s.gav).toSet ===
      Set(GAV("foobar", "bundle-foobar", "1.0")))
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 1)
    assert(lines.toSet === Set("foobar:bundle-foobar:1.0"))
    session.close()
  }

  test("snag directory with foobar jar recursive") {
    if (indexFile.exists()) { indexFile.delete() }
    val session = new SnagSession("*", indexFile, jarDir, true)
    assert(session.findArtifacts.map((s: Snaggable) => s.gav).toSet ===
      Set(
        GAV("foo", "bundle-foo", "1.0"),
        GAV("bar", "bundle-bar", "1.0"),
        GAV("foobar", "bundle-foobar", "1.0")
      )
    )
    val lines = Resource.fromFile(indexFile).lines().toList
    assert(lines.size === 3)
    assert(lines.toSet ===
      Set(
        "foo:bundle-foo:1.0",
        "bar:bundle-bar:1.0",
        "foobar:bundle-foobar:1.0"
      )
    )
    session.close()
  }
}