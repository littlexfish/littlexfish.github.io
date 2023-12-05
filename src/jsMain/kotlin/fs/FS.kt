package fs

import kotlinx.browser.window

object FS {

	val fs = window.navigator.asDynamic().storage
	var root: dynamic = null

	fun init() {
		if(fs != null && fs != undefined) {
			root = fs.getDirectory()
		}
	}

	fun getDirectoryRoot(): dynamic = root

}

