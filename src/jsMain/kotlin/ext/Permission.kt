package ext

import org.w3c.dom.events.Event
import org.w3c.dom.events.EventTarget

@Suppress("unused")
external class PermissionStatus : EventTarget {

	val name: String
	val state: String

}

@Suppress("unused")
fun PermissionStatus.onchange(callback: (Event) -> Unit) {
	this.addEventListener("change", callback)
}