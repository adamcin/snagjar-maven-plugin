package net.adamcin.maven.snagjar

import java.io.{InputStream, FileFilter, File}
import java.util.jar.{JarFile, JarEntry}
import scalax.io.{Codec, Resource, CloseAction}
import java.util.Properties
import collection.JavaConversions
import org.codehaus.plexus.util.SelectorUtils
import org.slf4j.{Logger, LoggerFactory}
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.model.{Dependency, Model}

/**
 *
 * @version $Id: SnagSession.java$
 * @author madamcin
 */
class SnagSession(val filter: String,
                  val indexFile: File,
                  val snagFile: File,
                  val recursive: Boolean) {

  val log: Logger = LoggerFactory.getLogger(getClass)

  lazy val tmpDir: File = {
    val tmp = File.createTempFile(getClass.getSimpleName, "")
    tmp.delete()
    new File(tmp.getParentFile, tmp.getName + ".dir")
  }

  val filterGav = (gav: GAV) => SelectorUtils.`match`(filter, gav.toString, true)

  def deleteRecursively(file: File) {
    if (file.isDirectory) {
      file.listFiles().foreach(deleteRecursively(_))
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
    log.error("[filterTeeIndex] file={}, gav={}", indexRes, s.gav.toString)
    indexRes.write(s.gav.toString + "\n")
    true
  }

  def findArtifacts: Stream[Snaggable] = Option(snagFile) match {
    case Some(file) => streamFromFile(file).filter((s: Snaggable) => filterGav(s.gav)).filter(filterTeeIndex(_))
    case None => Stream.empty[Snaggable]
  }

  private def streamFromFile(file: File): Stream[Snaggable] =
    if (!file.isDirectory) {
      streamFromJar(file)
    } else if (recursive) {
      streamFromDir(file)
    } else {
      Stream.empty[Snaggable]
    }

  private def streamFromFiles(files: Stream[File]): Stream[Snaggable] = {
    files match {
      case head #:: tail =>
        streamFromFile(head) #::: streamFromFiles(tail)
      case _ => Stream.empty[Snaggable]
    }
  }

  private def streamFromJar(jar: File): Stream[Snaggable] = {
    log.error("[streamFromJar] path={}", jar.getAbsolutePath)
    Option(Snaggable(jar, this)) match {
      case Some(snaggable) => Stream(snaggable)
      case None => Stream.empty[Snaggable]
    }
  }

  private def streamFromDir(dir: File): Stream[Snaggable] =
    streamFromFiles(dir.listFiles(SnagSession.DIR_FILTER).toStream)
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

  val metaFilter = (je: JarEntry) => {
    val matches = je.getName.startsWith(METADATA_PREFIX) && je.getName.endsWith(METADATA_SUFFIX)
    log.error("[metaFilter] entry={}, matches={}", je.getName, matches)
    matches
  }

  val pomFilter = (je: JarEntry) => je.getName.startsWith(METADATA_PREFIX) && je.getName.endsWith(POM_SUFFIX)
  val jarFilter = (je: JarEntry) => je.getName.endsWith(JAR_SUFFIX)

  val inputCloser = new CloseAction[InputStream] {
    protected def closeImpl(resource: InputStream) =
      try {
        resource.close()
        Nil
      } catch {
        case ex: Throwable => List(ex)
      }
  }

  def jarEntryOpener(jar: JarFile) = (entry: JarEntry) => jar.getInputStream(entry)

  val readGAV = (stream: (InputStream)) => {
    val props = new Properties()

    props.load(stream)

    (Option(props.getProperty(PROP_GROUP_ID)),
      Option(props.getProperty(PROP_ARTIFACT_ID)),
      Option(props.getProperty(PROP_VERSION))) match {

      case (Some(groupId), Some(artifactId), Some(version)) =>
        GAV(groupId, artifactId, version)
    }
  }

  def propsPathToPomPath(propsPath: String): String =
    propsPath.substring(0, propsPath.length - METADATA_SUFFIX.length) + POM_SUFFIX

  def extract(file: File, session: SnagSession): Snaggable = {

    log.error("[extract] file={}", file.getAbsolutePath)

    val jar = new JarFile(file)
    log.error("[extract] jar isEmpty?={} exists?={}", Option(jar).isEmpty, file.exists())
    val opener = jarEntryOpener(jar)

    val embeddedMetas =
      JavaConversions.enumerationAsScalaIterator(jar.entries()).filter(metaFilter)

    log.error("[extract] embeddedMetas is empty?={}", embeddedMetas.isEmpty)

    val extractedMetas = embeddedMetas.map((metaEntry: JarEntry) => {
      val pomEntry = jar.getJarEntry(propsPathToPomPath(metaEntry.getName))
      log.error("[extract] pomEntry={}", pomEntry.getName)
      val gav =
        Resource.fromInputStream(opener(metaEntry)).
          addCloseAction(inputCloser).acquireAndGet(readGAV)

      log.error("[extract] gav={}", gav)

      (Option(gav), Option(pomEntry)) match {
        case (Some(meta), Some(je)) => {
          val pom = session.createTempFile
          log.error("[extract] pomFile={}", pom.getAbsolutePath)
          Resource.fromFile(pom).doCopyFrom(Resource.fromInputStream(opener(pomEntry)))
          (meta, pom)
        }
      }
    }).toList

    log.error("[extract] extractedMetas={}", extractedMetas)

    // partial function application FTW!!!
    val extractor = extractDependencies(new MavenXpp3Reader)_
    val allDeps: Set[GAV] = extractedMetas.filter((m: (GAV, File)) => m match {
      case (gav: GAV, pom: File) => true
      case _ => false
    }).map(extractor).foldLeft(List.empty[GAV])((list, triple) => triple._3 ::: list).toSet

    log.error("[extract] allDeps={}", allDeps)

    extractedMetas.filterNot((meta: (GAV, File)) => allDeps.contains(meta._1)) match {
      case (gav, pom) :: Nil => new Snaggable(session, gav, file, pom)
    }
  }

  def extractDependencies(modelReader: MavenXpp3Reader)(extractedMeta: (GAV, File)): (GAV, File, List[GAV]) = {
    extractedMeta match {
      case (gav: GAV, pom: File) => {
        Resource.fromFile(pom).inputStream.addCloseAction(inputCloser).acquireFor(modelReader.read) match {
          case model: Model => {
            val deps = Option(model.getDependencies) match {
              case Some(deps) =>
                JavaConversions.collectionAsScalaIterable(deps).map((dep: Dependency) => {
                  GAV(dep.getGroupId, dep.getArtifactId, dep.getVersion)
                }).toList
              case _ => List.empty[GAV]
            }
            (gav, pom, deps) // return triple
          }
          case exs: List[Throwable] => log.error("[extractDependencies] Exception", exs(0)); null
        }
      }
    }
  }
}