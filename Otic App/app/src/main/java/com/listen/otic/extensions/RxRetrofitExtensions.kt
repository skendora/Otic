package com.listen.otic.extensions

import com.listen.otic.network.Outcome
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun <T> Observable<T>.subscribeForOutcome(onOutcome: (Outcome<T>) -> Unit): Disposable {
    return subscribe({ onOutcome(Outcome.success(it)) }, { onOutcome(processError(it)) })
}

private fun <T> processError(error: Throwable): Outcome<T> {
    return when (error) {
        is HttpException -> {
            val response = error.response()
            val body = response.errorBody()!!
            Outcome.apiError(getError(body, error))
        }
        is SocketTimeoutException, is IOException -> Outcome.failure(error)
        else -> Outcome.failure(error)
    }
}

private fun getError(
    responseBody: ResponseBody,
    throwable: Throwable
): Throwable {
    return try {
        val jsonObject = JSONObject(responseBody.string())
        Exception(jsonObject.getString("message"), throwable)
    } catch (e: Exception) {
        Exception(e.message ?: "$e")
    }
}
