package com.leysoft.core.kernel.context

import com.leysoft.core.kernel.context.data.*
import org.scalacheck.Gen

import java.time.ZonedDateTime

object ContextGenerator:
   val contextIdGen: Gen[ContextId]               =
     Gen.uuid.map(_.toString).map(ContextId(_))
   val contextCreatedAtGen: Gen[ContextCreatedAt] =
     Gen.const(ZonedDateTime.now).map(ContextCreatedAt(_))
   def gen: Gen[Context]                          =
     for
        id        <- contextIdGen
        createdAt <- contextCreatedAtGen
     yield Context(id, createdAt)
   def simple: Context                            = Context.make
