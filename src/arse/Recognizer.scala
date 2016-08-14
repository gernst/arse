// ARSE Parser libary
// (c) 2016 Gidon Ernst <gidonernst@gmail.com>
// This code is licensed under MIT license (see LICENSE for details)

package arse

import scala.language.implicitConversions

trait Recognizer[I] extends (I => I) {
  import Parser._
  import Recognizer._

  def |(that: Recognizer[I]): Recognizer[I] = accept {
    in =>
      this(in) or that(in)
  }

  def ~(that: Recognizer[I]): Recognizer[I] = accept {
    in0 =>
      val in1 = this(in0)
      val in2 = that(in1)
      in2
  }

  def ~[B](that: Parser[I, B]): Parser[I, B] = parse {
    in0 =>
      val in1 = this(in0)
      val (b, in2) = that(in1)
      (b, in2)
  }

  def ? = this | skip
  def * = accept { Recognizer.rep(this, _: I) }
  def + = this ~ this.*
  def !(msg: String) = this | accept(abort(msg, _))

  /*
  def $(): (I => Unit) = {
    in =>
      val out = this(in)
      if (!out.isEmpty) abort("expected end if input", out)
  }
  */
}

object Recognizer {
  def accept[I](f: I => (I)) = new Recognizer[I]() {
    def apply(in: I) = f(in)
  }

  implicit def tok[T](t: T): Recognizer[List[T]] = accept {
    case `t` :: in => in
    case _ => fail
  }

  def skip[I]: Recognizer[I] = accept {
    in => in
  }

  def rec[I](p: => Recognizer[I]): Recognizer[I] = accept {
    in => p(in)
  }

  def rep[I](p: Recognizer[I], in0: I): I = {
    val in1 = p(in0)
    val in2 = rep(p, in1)
    in2
  } or {
    in0
  }

}