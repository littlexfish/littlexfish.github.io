package app

import kotlinx.html.DIV

abstract class App(val name: String) {

	abstract fun buildGUI(): DIV.() -> Unit

	protected abstract fun onInit()

	protected abstract fun onRestore()

	protected open fun onResume() {}

	protected open fun onPause() {}

	protected abstract fun onSuspend()

	protected open fun onReceiveMessage(msg: String, extra: Map<String, String>?) {}

	private var isInit = false

	fun init() {
		if(!isInit) {
			isInit = true
			onInit()
		}
	}

	fun restore() {
		if(isInit) {
			onRestore()
		}
	}

	fun resume() {
		if(isInit) {
			onResume()
		}
	}

	fun pause() {
		if(isInit) {
			onPause()
		}
	}

	fun suspend() {
		if(isInit) {
			onSuspend()
		}
	}

	fun receiveMessage(msg: String, extra: Map<String, String>?) {
		onReceiveMessage(msg, extra)
	}

}