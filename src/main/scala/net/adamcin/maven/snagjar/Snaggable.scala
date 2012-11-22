package net.adamcin.maven.snagjar

import java.io.File

/**
 *
 * @version $Id: Snaggable.java$
 * @author madamcin
 */
class Snaggable(val session: SnagSession,
                val gav: GAV,
                val jar: File,
                val pom: File) {

}



object Snaggable {
  def apply(file: File, session: SnagSession): Snaggable = SnagSession.extract(file, session)
}