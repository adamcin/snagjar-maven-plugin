package net.adamcin.maven.snagjar


/**
 *
 * @version $Id: GAV.java$
 * @author madamcin
 */
case class GAV(groupId: String, artifactId: String, version: String) extends Ordered[GAV] {
  override val toString = List(groupId, artifactId, version).mkString(":")

  def compare(that: GAV) = {
    val g = this.groupId compare that.groupId
    if (g == 0) {
      val a = this.artifactId compare that.artifactId
      if (a == 0) {
        this.version compare that.version
      } else a
    } else g
  }
}