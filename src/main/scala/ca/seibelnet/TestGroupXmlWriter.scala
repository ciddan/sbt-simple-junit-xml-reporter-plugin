package ca.seibelnet

import collection.mutable.ListBuffer
import sbt.TestEvent
import java.net.InetAddress
import java.util.Date

import xml.XML
import org.scalatools.testing.{Result, Event}
import java.text.SimpleDateFormat

/**
 * User: bseibel
 * Date: 12-04-25
 * Time: 2:01 PM
 */


object TestGroupXmlWriter {

  def apply(name: String) = {
    new TestGroupXmlWriter(name)
  }
}


class TestGroupXmlWriter(val name: String) {

  var errors: Int = 0
  var failures: Int = 0
  var tests: Int = 0
  var skipped: Int = 0

  lazy val hostName = InetAddress.getLocalHost.getHostName

  case class TestEventData(testEvent: TestEvent, duration: Long)

  lazy val testEvents: ListBuffer[TestEventData] = new ListBuffer[TestEventData]


  def addEvent(testEvent: TestEvent, duration: Long) {
    testEvents += TestEventData(testEvent, duration)
    for (e: Event <- testEvent.detail) {
      tests += 1
      e.result() match {
        case Result.Failure => failures += 1
        case Result.Error => errors += 1
        case Result.Skipped => skipped += 1
        case _ => {}
      }
    }
  }


  def write(path: String) {

    val totalDuration = testEvents.head.duration.toDouble / 1000
    // hack - split up total duration to "mock" test duration
    val individualDuration = totalDuration / testEvents.map(_.testEvent.detail.size).sum

    val resultXml =
      <testSuite errors={errors.toString} failures={failures.toString} name={name} tests={tests.toString} time={totalDuration.toString} timestamp={new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date())}>
        <properties/>{for (e <- testEvents; t <- e.testEvent.detail) yield {
        <testcase classname={name} name={t.testName()} time={individualDuration.toString}>
          {t.result() match {
          case Result.Failure =>
            <failure message={t.error().getMessage} type={t.error().getClass.getName}>
              {t.error().getStackTrace.map {
              e => e.toString
            }.mkString("\n")}
            </failure>
          case Result.Error =>
            <error message={t.error().getMessage} type={t.error().getClass.getName}>
              {t.error().getStackTrace.map {
              e => e.toString
            }.mkString("\n")}
            </error>
          case Result.Skipped =>
              <skipped/>
          case _ => {}
        }}
        </testcase>
      }}<system-out></system-out>
        <system-err></system-err>
      </testSuite>

    XML.save(path + name + ".xml", resultXml, xmlDecl = true)

  }

}
