package io

import createElement
import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLElement
import org.w3c.dom.HTMLPreElement
import org.w3c.dom.css.CSSStyleDeclaration

class TerminalTunnel {

	private var onPipeIn: ((Element) -> Unit)? = null
	private var onPipeOut: ((Element) -> Unit)? = null

	private val pipeOutBuffer = ArrayList<Element>()
	private val pipeInBuffer = ArrayList<Element>()

	fun pipeIn(ele: Element) {
		if(onPipeIn != null) {
			onPipeIn!!(ele)
		}
		else {
			pipeInBuffer.add(ele)
		}
	}

	fun pipeOut(ele: Element) {
		if(onPipeOut != null) {
			onPipeOut!!(ele)
		}
		else {
			pipeOutBuffer.add(ele)
		}
	}

	fun registerPipeIn(onPipeIn: (Element) -> Unit) {
		this.onPipeIn = onPipeIn
		for(text in pipeInBuffer) {
			onPipeIn(text)
		}
		pipeInBuffer.clear()
	}

	fun registerPipeOut(onPipeOut: (Element) -> Unit) {
		this.onPipeOut = onPipeOut
		for(text in pipeOutBuffer) {
			onPipeOut(text)
		}
		pipeOutBuffer.clear()
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

fun TerminalTunnel.pipeOut(tag: String, builder: HTMLElement.() -> Unit) {
	pipeOut(createElement(tag, builder))
}

fun TerminalTunnel.pipeOutTag(tag: String, style: (CSSStyleDeclaration.() -> Unit)? = null) {
	val ele = document.createElement(tag) as HTMLElement
	if(style != null) {
		ele.style.style()
	}
	pipeOut(ele)
}

fun TerminalTunnel.pipeOutText(text: String, builder: (HTMLElement.() -> Unit)? = null) {
	pipeOut(createElement("span") {
		innerText = text
		builder?.invoke(this)
	})
}