package org.specs2
package control
import Throwablex._
import data.IncludedExcluded
import text.Trim._

/**
 * This trait Filters an Exception stacktrace
 */
trait StackTraceFilter {
  /** @return the filtered stacktrace */
  def apply(e: Seq[StackTraceElement]): Seq[StackTraceElement]
  /** @return an exception with a filtered stacktrace */
  def apply(e: Exception): Exception = exception(e.getMessage, apply(e.getFullStackTrace))
}

/**
 * Implementation of the StackTraceFilter trait with a list of include/exclude patterns
 */
case class IncludeExcludeStackTraceFilter(include: Seq[String], exclude: Seq[String]) extends StackTraceFilter { outer =>
  private val filter = new IncludedExcluded[StackTraceElement] {
    val matchFunction = (st: StackTraceElement, patterns: Seq[String]) => patterns.exists(p => st.toString matches (".*"+p+".*"))
    val include = outer.include
    val exclude = outer.exclude
  }
  /** filter an Exception stacktrace */
  def apply(st: Seq[StackTraceElement]) = st.filter(filter.keep)
}

/**
 * Factory object to build a stack trace filter
 */
object IncludeExcludeStackTraceFilter {
  def fromString(s: String): StackTraceFilter = {
    val splitted = s.split("/").toSeq
    if (splitted.size == 0)
      new IncludeExcludeStackTraceFilter(Seq[String](), Seq[String]())
    else if (splitted.size == 1)
      new IncludeExcludeStackTraceFilter(splitted(0).splitTrim(","), Seq[String]())
    else if (splitted.size == 2)
      new IncludeExcludeStackTraceFilter(splitted(0).splitTrim(","), splitted(1).splitTrim(","))
    else
      new IncludeExcludeStackTraceFilter(splitted(0).splitTrim(","), splitted.drop(1).mkString(",").splitTrim(","))
  }
}
/**
 * default filter for specs2 runs
 */
object DefaultStackTraceFilter extends
  IncludeExcludeStackTraceFilter(Seq(),
    Seq("org.specs2", "scalaz.concurrent", "java.util.concurrent", "sbt\\.", "com.intellij", "org.junit", "scala\\."))

