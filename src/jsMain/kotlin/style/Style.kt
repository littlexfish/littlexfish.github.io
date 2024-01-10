@file:JsFileName("style.js")
package style

import kotlinx.browser.document
import kotlinx.html.dom.create
import kotlinx.html.id
import kotlinx.html.js.style
import kotlinx.html.unsafe
import org.w3c.dom.HTMLStyleElement

abstract class StyleRegister(val name: String) {

	internal abstract fun getStyleContent(): String

}

object StyleRegistry {

	private val styles = mutableMapOf<String, StyleRegister>()
	private val loadedStyles = mutableSetOf<String>()
	private val elementMapping = mutableMapOf<String, HTMLStyleElement>()

	fun register(style: StyleRegister) {
		styles[style.name] = style
	}

	fun isRegistered(name: String): Boolean {
		return name in styles
	}

	fun loadStyle(name: String) {
		if(name in loadedStyles || name in elementMapping) {
			return
		}
		val style = styles[name] ?: throw IllegalArgumentException("Style $name not found")
		val content = style.getStyleContent().replace("[\\t\\n\\r]+".toRegex(), "")
		val styleElement = document.create.style {
			id = "style-$name"
			unsafe {
				+content
			}
		}
		document.head?.append(styleElement)
		elementMapping[name] = styleElement
		loadedStyles.add(name)
	}

	fun unloadStyle(name: String) {
		if(name !in loadedStyles || name !in elementMapping) {
			return
		}
		val element = elementMapping[name]
		element?.remove()
		elementMapping.remove(name)
		loadedStyles.remove(name)
	}

}