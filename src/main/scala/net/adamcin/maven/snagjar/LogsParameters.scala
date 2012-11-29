package net.adamcin.maven.snagjar

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.plugin.logging.Log

/**
 *
 * @version $Id: LogsParameters.java$
 * @author madamcin
 */
trait LogsParameters {
  def printParams(log: Log) {}
}