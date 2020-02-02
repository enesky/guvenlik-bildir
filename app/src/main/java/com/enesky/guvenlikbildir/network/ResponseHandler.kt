package com.enesky.guvenlikbildir.network

import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.util.*

/**
 * Created by Enes Kamil YILMAZ on 25.11.2019
 */

open class ResponseHandler : Observable() {

    fun <T : Any> handleSuccess(data: T): Result<T> {
        val resource: Result<T> = Result.success(data)
        setChanged()
        notifyObservers(resource)
        return resource
    }

    fun <T : Any> handleFailure(data: T) : Result<T> {
        val resource: Result<T> = Result.failure(data)
        setChanged()
        notifyObservers(resource)
        return resource
    }

    fun handleException(e: Exception) : Result<String> {
        val resource: Result<String> = when (e) {
            is HttpException -> Result.exception(getErrorMessage(e.code()))
            is SocketTimeoutException -> Result.exception(getErrorMessage(1))
            else -> {
                if (e.localizedMessage != null && e.localizedMessage!!.contains("proxy"))
                    Result.exception(getErrorMessage(2))
                else
                    Result.exception(getErrorMessage(Int.MAX_VALUE, e))
            }
        }

        setChanged()
        notifyObservers(resource)
        return resource
    }

    private fun getErrorMessage(code: Int?, e: Exception? = null): String {
        lateinit var errorMessage: String

        errorMessage = when (code) {
            1 -> "Timeout"
            2 -> "Failed to authenticate with proxy"
            401 -> "Unauthorized"
            404 -> "Not found"
            else -> "Something went wrong"
        }

        if (e != null && e.localizedMessage != null)
            errorMessage = e.localizedMessage!!

        return errorMessage
    }
}