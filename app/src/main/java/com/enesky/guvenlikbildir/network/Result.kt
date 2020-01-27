package com.enesky.guvenlikbildir.network

/*
sealed class Result<out T : Any, U>
class Success<out T : Any>(val data: T) : Result<T, Any?>()
class Error(val exception: Throwable, val message: String = exception.localizedMessage) : Result<Nothing, Any?>()
*/

data class Result<out T>(val status: Status, val data: T) {
    companion object {
        fun <T> success(data: T): Result<T> {
            return Result(Status.SUCCESS, data)
        }

        fun <T> failure(data: T): Result<T> {
            return Result(Status.FAILURE, data)
        }

        fun exception(exceptionMessage: String): Result<String> {
            return Result(Status.EXCEPTION, exceptionMessage)
        }
    }
}