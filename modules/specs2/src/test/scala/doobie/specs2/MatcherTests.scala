// Copyright (c) 2013-2018 Rob Norris and Contributors
// This software is licensed under the MIT License (MIT).
// For more information see LICENSE or https://opensource.org/licenses/MIT

package doobie.specs2

import cats.effect.{ ContextShift, IO, Concurrent }
import doobie.syntax.string._
import doobie.util.transactor.Transactor
import org.specs2.mutable.Specification
import scala.concurrent.ExecutionContext


trait MatcherChecks[M[_]] extends Specification
    with Checker[M]
    with AnalysisMatchers[M] {

  implicit def contextShift: ContextShift[M]
  def concurrent: Concurrent[M]

  lazy val transactor = Transactor.fromDriverManager[M](
    "org.h2.Driver",
    "jdbc:h2:mem:queryspec;DB_CLOSE_DELAY=-1",
    "sa", ""
  )(contextShift, concurrent)

  "valid query should pass" >> {
    sql"select 1".query[Int] must typecheck
  }

  "malformed sql should fail" >> {
    sql"not a valid sql".query[Int].must(not(typecheck))
  }

  "query with mismatched type should fail" >> {
    sql"select 'foo'".query[Int].must(not(typecheck))
  }
}

class IOMatcherCheck extends MatcherChecks[IO] with IOChecker {
  implicit def contextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)
  def concurrent = Concurrent[IO]
}
