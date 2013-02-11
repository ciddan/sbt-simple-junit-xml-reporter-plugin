package ca.seibelnet

import sbt._
import Keys._
import collection.mutable

/**
 * User: bseibel
 * Date: 12-04-25
 * Time: 12:02 PM
 */

object JUnitTestReporting extends Plugin {
  override def settings = Seq(
    testListeners += new JUnitTestListener("./target/test-reports/")
  )
}

class JUnitTestListener(val targetPath: String) extends TestReportListener {

  val output = new mutable.HashMap[String, TestGroupXmlWriter]()
  var currentName:String = null

  var start = System.currentTimeMillis()

  def testEvent(event: TestEvent) {
    val duration = System.currentTimeMillis() - start
    getOutput(currentName).addEvent(event, duration)
    start = System.currentTimeMillis()
  }

  def endGroup(name: String, result: TestResult.Value) {
    flushOutput(name)
  }

  def endGroup(name: String, t: Throwable) {
    flushOutput(name)
  }

  def startGroup(name: String) {
    getOutput(name)
    currentName = name
    start = System.currentTimeMillis()
  }

  private def getOutput(name: String): TestGroupXmlWriter = {
    this.synchronized {
      output.getOrElseUpdate(name, TestGroupXmlWriter(name))
    }
  }

  private def flushOutput(name: String) {
    val file = new File(targetPath)
    file.mkdirs()

    getOutput(name).write(targetPath)
  }

}
