package renetik.android.client.request

import renetik.android.java.collections.list
import renetik.android.java.extensions.collections.delete

class CSConcurrentResponse(private val responses: MutableList<CSResponse<*>>) : CSResponse<List<Any>>(list()) {

    private val runningResponses = list<CSResponse<*>>(responses)

    init {
        runningResponses.forEach { response ->
            response.onSuccess { onResponseSuccess(it) }
            response.onFailed { onResponseFailed(it) }
        }
    }

    private fun onResponseSuccess(response: CSResponse<*>) {
        if (runningResponses.apply { delete(response) }.isEmpty())
            success(list<Any>().apply { responses.forEach { add(it.data) } })
    }

    private fun onResponseFailed(response: CSResponse<*>) {
        responses.remove(response)
        responses.forEach { it.cancel() }
        failed(response)
    }

    override fun cancel() {
        responses.forEach { it.cancel() }
        super.cancel()
    }
}