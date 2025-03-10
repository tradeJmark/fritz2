package dev.fritz2.tracking

import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.render
import dev.fritz2.identification.Id
import dev.fritz2.test.initDocument
import dev.fritz2.test.runTest
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlin.test.Test
import kotlin.test.assertEquals

class TrackingTests {


    @Test
    fun testTracking() = runTest {
        initDocument()

        val transactionText = "long running"
        val transactionId = "transaction-${Id.next()}"

        val startValue = "start"
        val endValue = "end"
        val valueId = "value-${Id.next()}"

        val store = object : RootStore<String>(startValue) {
            val running = tracker()

            val longRunningHandler = handle {
                running.track(transactionText) {
                    delay(600)
                    endValue
                }
            }
        }

        render {
            div {
                span(id = transactionId) { store.running.data.map { if (it) transactionText else "" }.asText() }
                span(id = valueId) { store.data.asText() }
            }
        }
        delay(200)

        store.longRunningHandler()

        val valueBeforeTransaction = document.getElementById(valueId)?.textContent
        assertEquals(startValue, valueBeforeTransaction)

        delay(300)

        val transactionDuringHandler = document.getElementById(transactionId)?.textContent
        assertEquals(transactionText, transactionDuringHandler)

        val valueDuringTransaction = document.getElementById(valueId)?.textContent
        assertEquals(startValue, valueDuringTransaction)

        delay(450)

        val transactionAfterHandler = document.getElementById(transactionId)?.textContent
        assertEquals("", transactionAfterHandler)

        val valueAfterTransaction = document.getElementById(valueId)?.textContent
        assertEquals(endValue, valueAfterTransaction)

    }

    @Test
    fun testStopTrackingIfExceptionOccursDuringOperation() = runTest {
        initDocument()

        val resultElementId = "tracker-${Id.next()}"

        val store = object : RootStore<Int>(0) {
            val running = tracker()

            val handler = handle {
                try {
                    running.track {
                        delay(500)
                        throw Exception("Something unexpected happened")
                    }
                } catch (ex: Exception) {
                    // we just don't want to let this escape to the log...
                }
                it
            }
        }

        render {
            div {
                span(id = resultElementId) { store.running.data.map { if (it) "running" else "stopped" }.asText() }
            }
        }

        delay(100)

        store.handler()

        val valueBeforeTransaction = document.getElementById(resultElementId)?.textContent
        assertEquals("", valueBeforeTransaction)

        delay(200)

        val valueDuringTransaction = document.getElementById(resultElementId)?.textContent
        assertEquals("running", valueDuringTransaction)

        delay(500)

        val valueAfterTransaction = document.getElementById(resultElementId)?.textContent
        assertEquals("stopped", valueAfterTransaction)

    }
}