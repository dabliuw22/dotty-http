package com.leysoft.core.kernel.error

object data:
   abstract class BusinessError(message: String, code: String)
       extends RuntimeException(message)
