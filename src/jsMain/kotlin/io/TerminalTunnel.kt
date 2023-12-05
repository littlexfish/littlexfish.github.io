package io

import createElement
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