package com.leysoft.core.kernel.newtype

import cats.{Eq, Order, Show}
import cats.syntax.show.*
import squants.market.Currency
import squants.market.Money

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object instances:
   given Eq[ZonedDateTime] with
      override def eqv(
        x: ZonedDateTime,
        y: ZonedDateTime
      ): Boolean = x.equals(y)

   given Show[ZonedDateTime] with
      override def show(
        t: ZonedDateTime
      ): String = t.toString

   given Order[ZonedDateTime] with
      override def compare(
        x: ZonedDateTime,
        y: ZonedDateTime
      ): Int = x.compareTo(y)

   given Eq[BigDecimal] with
      override def eqv(
        x: BigDecimal,
        y: BigDecimal
      ): Boolean = x.equals(y)

   given Show[BigDecimal] with
      override def show(
        t: BigDecimal
      ): String = t.toString

   given Order[BigDecimal] with
      override def compare(
        x: BigDecimal,
        y: BigDecimal
      ): Int = x.compareTo(y)

   given Eq[Currency] with
      override def eqv(
        x: Currency,
        y: Currency
      ): Boolean = x.equals(y)

   given Show[Currency] with
      override def show(
        t: Currency
      ): String = t.toString

   given Order[Currency] with
      override def compare(
        x: Currency,
        y: Currency
      ): Int = x.name.compareTo(y.name)

   given Eq[Money] with
      override def eqv(
        x: Money,
        y: Money
      ): Boolean = x.equals(y)

   given Show[Money] with
      override def show(
        t: Money
      ): String = t.toString

   given Order[Money] with
      override def compare(
        x: Money,
        y: Money
      ): Int = x.compare(y)
