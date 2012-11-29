package net.adamcin.maven.snagjar

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.logging.Log
import org.apache.maven.plugins.annotations.Parameter

/**
 *
 * @version $Id: BaseMojo.java$
 * @author madamcin
 */
class BaseMojo extends AbstractMojo with LogsParameters {

  @Parameter(property = "debug")
  val debug = false

  def execute() {
    if (debug) {
      printParams(getLog)
    }
  }

  override def printParams(log: Log) {
    super.printParams(log)
    log.info("debug = " + debug)
  }
}