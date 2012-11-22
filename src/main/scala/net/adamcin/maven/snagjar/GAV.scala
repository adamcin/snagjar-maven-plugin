package net.adamcin.maven.snagjar


/**
 *
 * @version $Id: GAV.java$
 * @author madamcin
 */
case class GAV(groupId: String, artifactId: String, version: String) {
  override val toString = List(groupId, artifactId, version).mkString(":")
}