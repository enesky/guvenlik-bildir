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

    fun handleFailure(data: Any) : Result<*> {
        val resource: Result<*> =
            if (data is Exception) {
                when (data) {
                    is HttpException -> Result.failure(getErrorMessage(data.code()))
                    is SocketTimeoutException -> Result.failure(getErrorMessage(1))
                    else -> {
                        if (data.localizedMessage != null && data.localizedMessage!!.contains("proxy"))
                            Result.failure(getErrorMessage(2))
                        else
                            Result.failure(getErrorMessage(Int.MAX_VALUE, data))
                    }
                }
            } else {
                Result.failure(data)
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