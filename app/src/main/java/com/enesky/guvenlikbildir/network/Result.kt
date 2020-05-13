package com.enesky.guvenlikbildir.network

data class Result<out T>(val status: Status, val data: T) {
    companion object {
        fun <T> success(data: T): Result<T> {
            return Result(Status.SUCCESS, data)
        }

        fun <T> failure(data: T): Result<T> {
            return Result(Status.FAILURE, data)
        }

        /*
        fun exception(exceptionMessage: String): Result<String> {
            return Result(Status.EXCEPTION, exceptionMessage)
        }
         */
    }
}