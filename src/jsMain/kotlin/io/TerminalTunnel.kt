package io

import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLSpanElement

class TerminalTunnel(private val onTerminalOutput: (Element) -> Unit) {

	private var onTerminalInput: ((String) -> Unit)? = null

	fun inputFromTerminal(text: String) {
		onTerminalInput?.invoke(text)
	}

	fun outputToTerminal(text: String = "") {
		val span = document.createElement("span") as HTMLSpanElement
		span.innerText = text
		onTerminalOutput(span)
	}

	fun outputToTerminal(ele: Element) {
		onTerminalOutput(ele)
	}

	fun registerTerminalInput(onTerminalIn: (String) -> Unit) {
		this.onTerminalInput = onTerminalIn
	}

}