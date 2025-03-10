package dev.fritz2.dom.html

import dev.fritz2.binding.storeOf
import dev.fritz2.identification.Id
import dev.fritz2.test.initDocument
import dev.fritz2.test.runTest
import kotlinx.browser.document
import kotlinx.coroutines.delay
import org.w3c.dom.HTMLBodyElement
import org.w3c.dom.HTMLDivElement
import kotlin.test.Test
import kotlin.test.assertEquals

class RenderTests {

    @Test
    fun testShortRenderFunction() = runTest {
        initDocument()

        val store = storeOf(true)

        val divId = Id.next()

        render {
            section {
                store.data.render { value ->
                    div(id = divId) {
                        +if (value) "on" else "off"
                    }
                }
            }
        }

        delay(100)

        val div = document.getElementById(divId) as HTMLDivElement

        assertEquals("on", div.textContent)

        store.update(false)
        delay(200)

        val div2 = document.getElementById(divId) as HTMLDivElement

        assertEquals("off", div2.textContent)
    }

    @Test
    fun testRenderFunction() = runTest {
        document.clear()
        document.write("""<body id="target"><h1>Loading...</h1></body>""")

        val store = storeOf(true)

        val divId = Id.next()

        render("#target") {
            div(id = divId) {
                store.data.render { value ->
                    if (value) div { +"on" } else span { +"off" }
                }
            }
        }

        delay(100)

        val target = document.getElementById("target") as HTMLBodyElement
        assertEquals("target", target.id)
        assertEquals(1, target.childElementCount)
        assertEquals(divId, target.firstElementChild?.id)

        val div = document.getElementById(divId) as HTMLDivElement

        assertEquals(1, div.firstElementChild?.childElementCount)
        assertEquals("DIV", div.firstElementChild?.firstChild?.nodeName)
        assertEquals("on", div.firstElementChild?.textContent)

        store.update(false)
        delay(200)

        val span = document.getElementById(divId) as HTMLDivElement
        assertEquals(1, span.firstElementChild?.childElementCount)
        assertEquals("SPAN", span.firstElementChild?.firstChild?.nodeName)
        assertEquals("off", span.firstElementChild?.textContent)
    }
}