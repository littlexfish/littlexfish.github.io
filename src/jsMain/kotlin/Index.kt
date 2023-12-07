import kotlinx.html.TagConsumer
import kotlinx.html.a
import kotlinx.html.br
import kotlinx.html.id
import kotlinx.html.js.div
import kotlinx.html.js.input
import kotlinx.html.js.span
import org.w3c.dom.HTMLElement


fun TagConsumer<HTMLElement>.indexBuild() {
	div {
		id = "terminal"
		div {
			id = "terminal-output"
		}
		div {
			id = "terminal-input-outline"
			span {
				id = "terminal-input-prefix"
				+currentEnv.getCommandInputPrefix()
			}
			input {
				id = "terminal-input"
				autoFocus = true
			}
		}
	}
}

fun TagConsumer<HTMLElement>.indexError(msg: String) {
	div {
		id = "error-frame"
		+msg
		br
		+"If this is a bug, please open an "
		a("https://github.com/littlexfish/littlexfish.github.io/issues/new") {
			+"issue"
		}
		+"."
	}
}