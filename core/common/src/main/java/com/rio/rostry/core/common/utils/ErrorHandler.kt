package com.rio.rostry.core.common.utils

/**
 * ✅ Simple error handler interface
 */
interface ErrorHandler {
    fun getErrorMessage(throwable: Throwable): String
}

/**
 * ✅ Basic implementation of ErrorHandler
 */
class ErrorHandlerImpl : ErrorHandler {
    override fun getErrorMessage(throwable: Throwable): String {
        return throwable.message ?: "An unknown error occurred"
    }
}
