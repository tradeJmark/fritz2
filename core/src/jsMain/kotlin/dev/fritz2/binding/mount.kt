package dev.fritz2.binding

import dev.fritz2.lenses.LensException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

/**
 * collects the values of a given [Flow] one by one.
 * Use this for data-types that represent a single (simple or complex) value.
 *
 * @param parentJob parent Job for starting a new coroutine
 * @param upstream returns the Flow that should be mounted at this point
 * @param collect function which getting called when values are changing (rerender)
 */
inline fun <T> mountSimple(parentJob: Job, upstream: Flow<T>, crossinline collect: (T) -> Unit) {
    (MainScope() + parentJob).launch(start = CoroutineStart.UNDISPATCHED) {
        upstream.onEach { collect(it) }.catch {
            when (it) {
                is LensException -> {
                }
                else -> console.error(it)
            }
            // do not do anything here but canceling the coroutine, because this is an expected
            // behaviour when dealing with filtering, renderEach and idProvider
            cancel("error mounting", it)
        }.collect()
    }
}
