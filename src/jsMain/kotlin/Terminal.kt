import kotlinx.browser.document
import org.w3c.dom.Element
import org.w3c.dom.HTMLSpanElement


class Builder {

	var color: String? = null
	var backgroundColor: String? = null
	var bold: Boolean = false
	var italic: Boolean = false
	var underline: Boolean = false
	var strikethrough: Boolean = false
	var overline: Boolean = false

	var contents: ArrayList<Builder> = ArrayList()
	var text: String = ""

	constructor()

	constructor(text: String) {
		this.text = text
	}

	constructor(b: Builder.() -> Unit) {
		b()
	}

	fun append(b: Builder.() -> Unit) {
		val builder = Builder()
		builder.b()
		contents.add(builder)
	}

	fun build(): Element {
		val span = document.createElement("span") as HTMLSpanElement
		color?.let { span.style.color = it }
		backgroundColor?.let { span.style.backgroundColor = it }
		if(bold) {
			span.style.fontWeight = "bold"
		}
		if(italic) {
			span.style.fontStyle = "italic"
		}
		var dec = ""
		if(underline) {
			dec += "underline "
		}
		if(strikethrough) {
			dec += "line-through "
		}
		if(overline) {
			dec += "overline "
		}
		if(dec.isNotEmpty()) {
			span.style.textDecoration = dec
		}

		if(contents.isEmpty()) {
			span.innerText = text
		}
		else {
			for(content in contents) {
				span.append(content.build())
			}
		}
		return span
	}

}