package dev.fritz2.dom

import dev.fritz2.binding.Patch
import dev.fritz2.binding.Store
import dev.fritz2.binding.mountSimple
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.html.Scope
import dev.fritz2.dom.html.TagContext
import dev.fritz2.lenses.IdProvider
import kotlinx.browser.window
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.w3c.dom.Element

/**
 * A marker to separate the layers of calls in the type-safe-builder pattern.
 */
@DslMarker
annotation class HtmlTagMarker

/**
 * Represents a tag in the resulting HTML.
 * Sorry for the name, but we needed to delimit it from the [Element] it is wrapping.
 *
 * @param tagName name of the tag. Used to create the corresponding [Element]
 * @property id the DOM-id of the element to be created
 * @property baseClass a static base value for the class-attribute.
 * All dynamic values for this attribute will be concatenated to this base-value.
 * @property job used for launching coroutines in
 * @property scope set some arbitrary scope entries into the [Tag]'s scope
 * @property domNode the [Element]-instance that is wrapped by this [Tag]
 * (you should never have to pass this by yourself, just let it be created by the default)
 */
@HtmlTagMarker
open class Tag<out E : Element>(
    private val tagName: String,
    val id: String? = null,
    val baseClass: String? = null,
    override val job: Job,
    override val scope: Scope,
    override val domNode: E = window.document.createElement(tagName).also { element ->
        if (id != null) element.id = id
        if (!baseClass.isNullOrBlank()) element.className = baseClass
    }.unsafeCast<E>()
) : WithDomNode<E>, WithComment<E>, EventContext<E>, TagContext {

    /**
     * Creates the content of the [Tag] and appends it as a child to the wrapped [Element].
     *
     * @param element the parent element of the new content
     * @param content lambda building the content (following the type-safe-builder pattern)
     */
    override fun <E : Element, W : WithDomNode<E>> register(element: W, content: (W) -> Unit): W {
        content(element)
        domNode.appendChild(element.domNode)
        return element
    }

    /**
     * Renders the data of a [Flow] as [Tag]s to the DOM.
     *
     * @receiver [Flow] containing the data
     * @param into target to mount content to. If not set a child [DIV] is added to the [Tag] this method is called on
     * @param content [RenderContext] for rendering the data to the DOM
     */
    inline fun <V> Flow<V>.render(into: RenderContext? = null, crossinline content: RenderContext.(V) -> Unit) =
        mount(into, this, content)


    /**
     * Renders each element of a [Flow]s content.
     * Internally the [Patch]es are determined using Myer's diff-algorithm.
     * This allows the detection of moves. Keep in mind, that no [Patch] is derived,
     * when an element stays the same, but changes its internal values.
     *
     * @param idProvider function to identify a unique entity in the list
     * @param into target to mount content to. If not set a child [DIV] is added to the [Tag] this method is called on
     * @param content [RenderContext] for rendering the data to the DOM
     */
    inline fun <V> Flow<List<V>>.renderEach(
        noinline idProvider: IdProvider<V, *>? = null,
        into: RenderContext? = null,
        crossinline content: RenderContext.(V) -> RenderContext
    ) =
        mount(into, this, idProvider, content)

    /**
     * Renders each element of a [Store]s [List] content.
     * Internally the [Patch]es are determined using Myer's diff-algorithm.
     * This allows the detection of moves. Keep in mind, that no [Patch] is derived,
     * when an element stays the same, but changes its internal values.
     *
     * @param idProvider function to identify a unique entity in the list
     * @param into target to mount content to. If not set a child [DIV] is added to the [Tag] this method is called on
     * @param content [RenderContext] for rendering the data to the DOM
     */
    inline fun <V> Store<List<V>>.renderEach(
        noinline idProvider: IdProvider<V, *>,
        into: RenderContext? = null,
        crossinline content: RenderContext.(Store<V>) -> RenderContext
    ) =
        mount(into, this, idProvider, content)

    /**
     * Renders each element of a [Store]s list content.
     * Internally the [Patch]es are determined using the position of an item in the list.
     * Moves cannot be detected that way and replacing an item at a certain position will be treated as a change of the item.
     *
     * @param content [RenderContext] for rendering the data to the DOM given a [Store] of the list's item-type
     * @param into target to mount content to. If not set a child [DIV] is added to the [Tag] this method is called on
     */
    inline fun <V> Store<List<V>>.renderEach(
        into: RenderContext? = null,
        crossinline content: RenderContext.(Store<V>) -> RenderContext
    ) =
        mount(into, this, content)


    /**
     * Sets an attribute.
     *
     * @param name to use
     * @param value to use
     */
    fun attr(name: String, value: String) {
        domNode.setAttribute(name, value)
    }

    /**
     * Sets an attribute only if its [value] is not null.
     *
     * @param name to use
     * @param value to use
     */
    fun attr(name: String, value: String?) {
        value?.let { domNode.setAttribute(name, it) }
    }

    /**
     * Sets an attribute.
     *
     * @param name to use
     * @param value to use
     */
    fun attr(name: String, value: Flow<String>) {
        mountSimple(job, value) { v -> attr(name, v) }
    }

    /**
     * Sets an attribute only for all none null values of the flow.
     *
     * @param name to use
     * @param value to use
     */
    fun attr(name: String, value: Flow<String?>) {
        mountSimple(job, value) { v ->
            if (v != null) attr(name, v)
            else domNode.removeAttribute(name)
        }
    }

    /**
     * Sets an attribute.
     *
     * @param name to use
     * @param value to use
     */
    fun <T> attr(name: String, value: T) {
        value?.let { domNode.setAttribute(name, it.toString()) }
    }

    /**
     * Sets an attribute.
     *
     * @param name to use
     * @param value to use
     */
    fun <T> attr(name: String, value: Flow<T>) {
        mountSimple(job, value.map { it?.toString() }) { v ->
            if (v != null) attr(name, v)
            else domNode.removeAttribute(name)
        }
    }

    /**
     * Sets an attribute when [value] is true otherwise removes it.
     *
     * @param name to use
     * @param value for decision
     * @param trueValue value to use if attribute is set (default "")
     */
    fun attr(name: String, value: Boolean, trueValue: String = "") {
        if (value) domNode.setAttribute(name, trueValue)
        else domNode.removeAttribute(name)
    }

    /**
     * Sets an attribute when [value] is true otherwise removes it.
     *
     * @param name to use
     * @param value for decision
     * @param trueValue value to use if attribute is set (default "")
     */
    fun attr(name: String, value: Boolean?, trueValue: String = "") {
        value?.let {
            if (it) domNode.setAttribute(name, trueValue)
            else domNode.removeAttribute(name)
        }
    }

    /**
     * Sets an attribute when [value] is true otherwise removes it.
     *
     * @param name to use
     * @param value for decision
     * @param trueValue value to use if attribute is set (default "")
     */
    fun attr(name: String, value: Flow<Boolean>, trueValue: String = "") {
        mountSimple(job, value) { v -> attr(name, v, trueValue) }
    }

    /**
     * Sets an attribute when [value] is true otherwise removes it.
     *
     * @param name to use
     * @param value for decision
     * @param trueValue value to use if attribute is set (default "")
     */
    fun attr(name: String, value: Flow<Boolean?>, trueValue: String = "") {
        mountSimple(job, value) { v -> attr(name, v, trueValue) }
    }

    /**
     * Sets an attribute from a [List] of [String]s.
     * Therefore it concatenates the [String]s to the final value [String].
     *
     * @param name to use
     * @param values for concatenation
     * @param separator [String] for separation
     */
    fun attr(name: String, values: List<String>, separator: String = " ") {
        domNode.setAttribute(name, values.joinToString(separator))
    }

    /**
     * Sets an attribute from a [List] of [String]s.
     * Therefore it concatenates the [String]s to the final value [String].
     *
     * @param name to use
     * @param values for concatenation
     * @param separator [String] for separation
     */
    fun attr(name: String, values: Flow<List<String>>, separator: String = " ") {
        mountSimple(job, values) { v -> attr(name, v, separator) }
    }

    /**
     * Sets an attribute from a [Map] of [String]s and [Boolean]s.
     * The key inside the [Map] getting only set when the corresponding value
     * is true. Otherwise they get removed from the resulting [String].
     *
     * @param name to use
     * @param values to use
     * @param separator [String] for separation
     */
    fun attr(name: String, values: Map<String, Boolean>, separator: String = " ") {
        domNode.setAttribute(name, values.filter { it.value }.keys.joinToString(separator))
    }

    /**
     * Sets an attribute from a [Map] of [String]s and [Boolean]s.
     * The key inside the [Map] getting only set when the corresponding value
     * is true. Otherwise they get removed from the resulting [String].
     *
     * @param name to use
     * @param values to use
     * @param separator [String] for separation
     */
    fun attr(name: String, values: Flow<Map<String, Boolean>>, separator: String = " ") {
        mountSimple(job, values) { v -> attr(name, v, separator) }
    }

    private fun setClassName(className: String): String =
        when {
            baseClass.isNullOrBlank() -> className
            className.isNotBlank() -> "$baseClass $className"
            else -> baseClass
        }

    /**
     * Sets the *class* attribute.
     *
     * @param value as [String]
     */
    fun className(value: String) {
        attr("class", setClassName(value))
    }

    /**
     * Sets the *class* attribute.
     *
     * @param value [Flow] with [String]
     */
    fun className(value: Flow<String>) {
        attr("class", value.map { setClassName(it) })
    }

    /**
     * Sets the *class* attribute from a [List] of [String]s.
     *
     * @param values as [List] of [String]s
     */
    fun classList(values: List<String>) {
        attr("class", if (baseClass.isNullOrBlank()) values else values + baseClass)
    }

    /**
     * Sets the *class* attribute from a [List] of [String]s.
     *
     * @param values [Flow] with [List] of [String]s
     */
    fun classList(values: Flow<List<String>>) {
        attr("class", if (baseClass.isNullOrBlank()) values else values.map { it + baseClass })
    }

    /**
     * Sets the *class* attribute from a [Map] of [String] to [Boolean].
     * If the value of the [Map]-entry is true, the key will be used inside the resulting [String].
     *
     * @param values as [Map] with key to set and corresponding values to decide
     */
    fun classMap(values: Map<String, Boolean>) {
        attr("class", if (baseClass.isNullOrBlank()) values else values + (baseClass to true))
    }

    /**
     * Sets the *class* attribute from a [Map] of [String] to [Boolean].
     * If the value of the [Map]-entry is true, the key will be used inside the resulting [String].
     *
     * @param values [Flow] of [Map] with key to set and corresponding values to decide
     */
    fun classMap(values: Flow<Map<String, Boolean>>) {
        attr("class", if (baseClass.isNullOrBlank()) values else values.map { it + (baseClass to true) })
    }

    /**
     * Sets the *style* attribute.
     *
     * @param value [String] to set
     */
    fun inlineStyle(value: String) {
        attr("style", value)
    }

    /**
     * Sets the *style* attribute.
     *
     * @param value [Flow] with [String]
     */
    fun inlineStyle(value: Flow<String>) {
        attr("style", value)
    }

    /**
     * Sets the *style* attribute from a [List] of [String]s.
     *
     * @param values [List] of [String]s
     */
    fun inlineStyle(values: List<String>) {
        attr("style", values, separator = "; ")
    }

    /**
     * Sets the *style* attribute from a [List] of [String]s.
     *
     * @param values [Flow] with [List] of [String]s
     */
    fun inlineStyle(values: Flow<List<String>>) {
        attr("style", values, separator = "; ")
    }

    /**
     * Sets the *style* attribute from a [Map] of [String] to [Boolean].
     * If the value of the [Map]-entry is true, the key will be used inside the resulting [String].
     *
     * @param values [Map] with key to set and corresponding values to decide
     */
    fun inlineStyle(values: Map<String, Boolean>) {
        attr("style", values, separator = "; ")
    }

    /**
     * Sets the *style* attribute from a [Map] of [String] to [Boolean].
     * If the value of the [Map]-entry is true, the key will be used inside the resulting [String].
     *
     * @param values [Flow] of [Map] with key to set and corresponding values to decide
     */
    fun inlineStyle(values: Flow<Map<String, Boolean>>) {
        attr("style", values, separator = "; ")
    }

    /**
     * Sets all scope-entries as data-attributes to the element.
     */
    fun Scope.asDataAttr() {
        for ((k, v) in this) {
            attr("data-${k.name}", v.toString())
        }
    }

    /**
     * Sets scope-entry for the given [key] as data-attribute to the element
     * when available.
     *
     * @param key key of scope-entry to look for in scope
     */
    fun <T : Any> Scope.asDataAttr(key: Scope.Key<T>) {
        this[key]?.let {
            attr("data-${key.name}", it.toString())
        }
    }
}
