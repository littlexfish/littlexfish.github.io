package command

class Argument {

	private val args = mutableMapOf<String, MutableList<String>>()
	private val standalone = mutableListOf<String>()

	constructor(arr: Array<String>, needValue: List<String> = emptyList()) {
		parse(arr, needValue)
	}

	private fun parse(arr: Array<String>, needValue: List<String>) {
		var idx = 0
		val addNextValue = { key: String, index: Int ->
			if (needValue.contains(key) && index + 1 < arr.size && !arr[index + 1].startsWith("-")) {
				val v = arr[index + 1]
				if (args.containsKey(key)) {
					args[key]!!.add(v)
				} else {
					args[key] = mutableListOf(v)
				}
				idx++
			}
			else {
				args[key] = mutableListOf()
			}
		}
		while(idx < arr.size) {
			val current = arr[idx]
			if(!current.startsWith("-")) {
				standalone.add(current)
				idx++
			}
			else {
				if(current.startsWith("--")) {
					val key = current.substring(2)
					addNextValue(key, idx)
				}
				else {
					val key = current.substring(1)
					addNextValue(key, idx)
				}
			}
			idx++
		}
	}

	fun get(key: String): String? {
		return args[key]?.lastOrNull()
	}

	fun has(key: String): Boolean {
		return args.containsKey(key)
	}

	fun getStandalone(): List<String> {
		return standalone
	}

	fun gets(key: String): List<String>? {
		return args[key]
	}

	fun getOrDefault(key: String, default: String): String {
		return get(key) ?: default
	}

	fun getAsInt(key: String): Int? {
		return get(key)?.toIntOrNull()
	}

	fun getAsIntOrDefault(key: String, default: Int): Int {
		return getAsInt(key) ?: default
	}

	fun getAsFloat(key: String): Float? {
		return get(key)?.toFloatOrNull()
	}

	fun getAsFloatOrDefault(key: String, default: Float): Float {
		return getAsFloat(key) ?: default
	}

}