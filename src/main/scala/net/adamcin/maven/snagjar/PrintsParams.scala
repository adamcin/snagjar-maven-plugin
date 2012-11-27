package net.adamcin.maven.snagjar

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter

/**
 *
 * @version $Id: PrintsParams.java$
 * @author madamcin
 */
trait PrintsParams extends AbstractMojo {

  @Parameter(property = "debug")
  val debug = false

  def execute() {
   if (debug) {
     printParams()
   }
  }

  def printParams() {}
}