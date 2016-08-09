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

import org.apache.maven.artifact.versioning.DefaultArtifactVersion

/**
 * Case class representing basic maven artifact coordinates
 * @since 0.8.0
 * @author Mark Adamcin
 */
case class GAV(groupId: String,
               artifactId: String,
               version: String, parent: Option[GAV] = None)
  extends Ordered[GAV] {

  val parsedVersion = Option(version) match {
    case Some(v) => new DefaultArtifactVersion(version)
    case None => new DefaultArtifactVersion("")
  }

  override val toString = List(groupId, artifactId, version).mkString(":")

  def compareNoVersion(that: GAV) = {
    val g = this.groupId compare that.groupId
    if (g == 0) {
      this.artifactId compare that.artifactId
    } else g
  }

  def compare(that: GAV) = {
    val a = this.compareNoVersion(that)
    if (a == 0) {
      this.parsedVersion compareTo that.parsedVersion
    } else a
  }

  /**
    * @since 1.2.0
    * @return a snapshot version of the GAV to support transient pom generation
    */
  def toSnapshot(): GAV = {

    this.copy(version = this.version.split("-")(0)+"-SNAPSHOT", parent = None)
  }

  def min(that: GAV) = if (this < that) this else that
  def max(that: GAV) = if (this >= that) this else that
}