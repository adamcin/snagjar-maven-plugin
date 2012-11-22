package net.adamcin.maven.snagjar

import java.util.Properties

/**
 *
 * @version $Id: GAV.java$
 * @author madamcin
 */
case class GAV(groupId: String, artifactId: String, version: String) {
  override val toString = List(groupId, artifactId, version).mkString(":")
}