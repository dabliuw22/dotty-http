package com.leysoft.core.kernel.context

import com.leysoft.core.kernel.context.data.*
import org.scalacheck.Gen

import java.time.ZonedDateTime

object ContextGenerator:
   val contextIdGen: Gen[ContextId] =
     Gen.uuid.map(_.toString).map(ContextId(_))
   def gen: Gen[Context]            =
     for id <- contextIdGen
     yield Context(id)
   def simple: Context              = Context.make
