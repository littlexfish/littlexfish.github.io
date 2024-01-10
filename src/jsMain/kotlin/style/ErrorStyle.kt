package style

import fs.SettKeys
import fs.Settings

object ErrorStyle : StyleRegister("error") {

	override fun getStyleContent(): String {
		var error = Settings[SettKeys.Theme.COLOR_ERROR]
		if(error.isBlank()) error = "#ff0000"
		var color1 = Settings[SettKeys.Theme.COLOR_1]
		if(color1.isBlank()) color1 = "#6495ed"
		return """
		#error-frame {
			display: block;
			border: $error 1px solid;
			border-radius: 5px;
			$CENTER_ELEMENT
			padding: 20px 50px;
			font-size: 30px;
			max-width: 80%;
			max-height: 80%;
			overflow: auto;
		}
		#error-frame a {
			text-decoration: none;
			color: $color1;
		}
		#error-frame a:hover {
			text-decoration: underline;
		}
		"""
	}

}