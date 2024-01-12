package util

object GlobalState {

	private val states = HashMap<String, Any>()

	fun <T> get(key: String): T? {
		return states[key].unsafeCast<T>()
	}

	fun set(key: String, value: Any) {
		states[key] = value
	}

}