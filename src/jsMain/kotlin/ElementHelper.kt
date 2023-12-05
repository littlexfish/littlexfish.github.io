import kotlinx.browser.document
import org.w3c.dom.HTMLElement

fun createElement(tag: String, content: HTMLElement.() -> Unit): HTMLElement {
	return (document.createElement(tag) as HTMLElement).apply(content)
}