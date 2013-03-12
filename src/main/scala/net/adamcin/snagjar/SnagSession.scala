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

import java.io.{InputStream, FileFilter, File}
import java.util.jar.{JarFile, JarEntry}
import scalax.io.{Resource, CloseAction}
import java.util.{Collections, Properties}
import collection.JavaConversions._
import org.codehaus.plexus.util.SelectorUtils
import org.slf4j.{Logger, LoggerFactory}
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.{Dependency, Model}

/**
 * Class that performs the directory scanning and artifact snagging
 * @param filter basic globbing filter to be matched against maven coordinates (groupId:artifactId:version)
 * @param indexFile file to which the snagged maven coordinates will be written
 * @param snagFile file representing the directory or single file to be snagged during this session
 * @param recursive whether to recurse into a snagFile directory
 * @since 0.8.0
 * @author Mark Adamcin
 */
class SnagSession(val filter: String,
                  val indexFile: File,
                  val snagFile: File,
                  val recursive: Boolean) {

  val log = LoggerFactory.getLogger(getClass)

  lazy val tmpDir: File = {
    val tmp = File.createTempFile(getClass.getSimpleName, "")
    tmp.delete()
    new File(tmp.getParentFile, tmp.getName + ".dir")
  }

  val filterGav = (gav: GAV) => SelectorUtils.`match`(filter, gav.toString, true)

  def deleteRecursively(file: File) {
    if (file.isDirectory) {
      file.listFiles() foreach { deleteRecursively }
    }
    file.delete()
  }

  def createTempFile: File = {
    tmpDir.mkdir()
    File.createTempFile("snag", ".tmp", tmpDir)
  }

  def close() { deleteRecursively(tmpDir) }

  val indexRes = Resource.fromFile(indexFile)
  def filterTeeIndex(s: Snaggable): Boolean = {
    indexRes.write(s.gav.toString + "\n")
    true
  }

  def findArtifacts: Stream[Snaggable] = Option(snagFile) match {
    case Some(file) => streamFromFile(file).filter { s => filterGav(s.gav) }.filter { filterTeeIndex }
    case None => Stream.empty[Snaggable]
  }

  private def streamFromFile(file: File): Stream[Snaggable] = {
    if (!file.isDirectory) {
      streamFromJar(file)
    } else {
      streamFromDir(file)
    }
  }

  private def streamFromFiles(files: Stream[File]): Stream[Snaggable] = {
    files match {
      case head #:: tail =>
        streamFromFile(head) #::: streamFromFiles(tail)
      case _ => Stream.empty[Snaggable]
    }
  }

  private def streamFromJar(jar: File): Stream[Snaggable] = {
    Option(Snaggable(jar, this)) match {
      case Some(snaggable) => Stream(snaggable)
      case None => Stream.empty[Snaggable]
    }
  }

  private def streamFromDir(dir: File): Stream[Snaggable] =
    streamFromFiles(dir.listFiles(SnagSession.DIR_FILTER).toStream.filter((f: File) => recursive || !f.isDirectory))
}

object SnagSession {
  val log: Logger = LoggerFactory.getLogger(getClass)

  val EMBEDDED_PREFIX = "embedded"
  val JAR_SUFFIX = ".jar"

  val DIR_FILTER = new FileFilter {
    def accept(pathname: File) =
      pathname.isDirectory || pathname.getName.endsWith(JAR_SUFFIX)
  }

  val METADATA_PREFIX = "META-INF/maven/"
  val METADATA_SUFFIX = "/pom.properties"
  val POM_SUFFIX = "/pom.xml"

  val PROP_GROUP_ID = "groupId"
  val PROP_ARTIFACT_ID = "artifactId"
  val PROP_VERSION = "version"

  val metaFilter = (je: JarEntry) => je.getName.startsWith(METADATA_PREFIX) && je.getName.endsWith(METADATA_SUFFIX)
  val pomFilter = (je: JarEntry) => je.getName.startsWith(METADATA_PREFIX) && je.getName.endsWith(POM_SUFFIX)

  val inputCloser = new CloseAction[InputStream] {
    protected def closeImpl(resource: InputStream) =
      try {
        resource.close()
        Nil
      } catch {
        case ex: Throwable => List(ex)
      }
  }

  def jarEntryOpener(jar: JarFile)(entry: JarEntry) = jar.getInputStream(entry)

  val readGAV = (stream: (InputStream)) => {
    val props = new Properties()

    props.load(stream)

    (Option(props.getProperty(PROP_GROUP_ID)),
      Option(props.getProperty(PROP_ARTIFACT_ID)),
      Option(props.getProperty(PROP_VERSION))) match {

      case (Some(groupId), Some(artifactId), Some(version)) => GAV(groupId, artifactId, version)
      case _ => null
    }
  }

  def propsPathToPomPath(propsPath: String): String =
    propsPath.substring(0, propsPath.length - METADATA_SUFFIX.length) + POM_SUFFIX

  // TODO should return Option[Snaggable] instead and use flatMap on the resulting sequence
  def extract(file: File, session: SnagSession): Snaggable = {
    val jar = new JarFile(file)
    val opener = jarEntryOpener(jar)_

    val embeddedMetas = jar.entries().filter(metaFilter)

    val extractedMetas = embeddedMetas.map((metaEntry: JarEntry) => {
      val pomEntry = jar.getJarEntry(propsPathToPomPath(metaEntry.getName))
      val gav =
        Resource.fromInputStream(opener(metaEntry)).
          addCloseAction(inputCloser).acquireAndGet(readGAV)

      (Option(gav), Option(pomEntry)) match {
        case (Some(meta), Some(je)) => {
          val pom = session.createTempFile
          Resource.fromFile(pom).doCopyFrom(Resource.fromInputStream(opener(pomEntry)))
          (meta, pom)
        }
        case _ => null
      }
    }).toList

    // partial function application FTW!!!
    val extractor = extractDependencies(new MavenXpp3Reader)_

    val allDeps: Set[GAV] = extractedMetas.filter {

      _ match {
        case (gav: GAV, pom: File) => true
        case _ => false
      }

    }.map(extractor).foldLeft(List.empty[GAV]) {

      // fold left to build a combined set of all dependencies' GAVs
      (list, triple) => triple._3 ::: list

    }.toSet

    extractedMetas.filterNot { meta: (GAV, File) => allDeps contains meta._1 } match {
      case (gav, pom) :: Nil => new Snaggable(session, gav, file, pom)
      case _ => null
    }
  }

  def applyReader(modelReader: MavenXpp3Reader)(in: InputStream): Model = modelReader.read(in)

  def extractDependencies(modelReader: MavenXpp3Reader)(extractedMeta: (GAV, File)): (GAV, File, List[GAV]) =
    extractedMeta match {
      case (gav: GAV, pom: File) => {

        val modelIn = Resource.fromFile(pom).inputStream acquireFor { applyReader(modelReader)_ }

        val deps = modelIn match {
          case Right(model) =>

            val depIt = Option(model.getDependencies).getOrElse(Collections.emptyList[Dependency])

            depIt.map { dep => GAV(dep.getGroupId, dep.getArtifactId, dep.getVersion) }.toList

          case Left(exs) => List.empty[GAV] // TODO: Handle exceptions
        }

        (gav, pom, deps) // return triple
      }
      case _ => null
    }

}