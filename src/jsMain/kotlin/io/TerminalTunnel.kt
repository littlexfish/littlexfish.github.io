package io

import Translation
import createElement
import fs.SettKeys
import fs.Settings
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.css.CSSStyleDeclaration

class TerminalTunnel {

	private var onPipeOut: ((Element) -> Unit)? = null

	private val pipeOutBuffer = ArrayList<Element>()
	private val pipeInBuffer = ArrayList<Element>()

	fun pipeIn(ele: Element) {
		pipeInBuffer.add(ele)
	}

	fun pipeOut(ele: Element) {
		if(onPipeOut != null) {
			onPipeOut!!(ele)
		}
		else {
			pipeOutBuffer.add(ele)
		}
	}

	fun registerPipeOut(onPipeOut: (Element) -> Unit) {
		this.onPipeOut = onPipeOut
		for(text in pipeOutBuffer) {
			onPipeOut(text)
		}
		pipeOutBuffer.clear()
	}

	fun readFromPipeIn(): Element? {
		return pipeInBuffer.removeFirstOrNull()
	}

	fun hasNextRead(): Boolean {
		return pipeInBuffer.isNotEmpty()
	}

}

fun TerminalTunnel.pipeOutPre(content: String, style: (CSSStyleDeclaration.() -> Unit)? = null) {
	val pre = document.createElement("pre") as HTMLPreElement
	pre.innerText = content
	if(style != null) {
		pre.style.style()
	}
	pipeOut(pre)
}

fun TerminalTunnel.pipeOutTag(tag: String, build: (HTMLElement.() -> Unit)? = null) {
	val ele = document.createElement(tag) as HTMLElement
	if(build != null) {
		ele.build()
	}
	pipeOut(ele)
}

fun TerminalTunnel.pipeOutText(text: String, builder: (HTMLElement.() -> Unit)? = null) {
	pipeOut(createElement("span") {
		innerText = text
		builder?.invoke(this)
	})
}

fun TerminalTunnel.pipeOutNewLine() {
	pipeOutTag("br")
}

fun TerminalTunnel.pipeOutTextLn(text: String, builder: (HTMLElement.() -> Unit)? = null) {
	pipeOutText(text, builder)
	pipeOutNewLine()
}

fun TerminalTunnel.pipeOutLink(href: String, target: String = "_blank", builder: (HTMLElement.() -> Unit)? = null) {
	pipeOutTag("a") {
		asDynamic().href = href
		asDynamic().target = target
		builder?.invoke(this)
	}
}

fun TerminalTunnel.pipeOutErrorText(error: String) {
	pipeOutText(error) {
		style.color = Settings.getSettings(SettKeys.Theme.COLOR_ERROR)
	}
}

fun TerminalTunnel.pipeOutErrorTextTr(id: String, vararg args: Pair<String, Any?>) {
	pipeOutErrorText(Translation[id, mapOf(*args)])
}
