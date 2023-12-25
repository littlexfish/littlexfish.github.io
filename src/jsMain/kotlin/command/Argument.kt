package command

/**
 * Argument parser
 */
class Argument(arr: Array<String>, needValue: List<String> = emptyList()) {

	private val args = mutableMapOf<String, MutableList<String>>()
	private val standalone = mutableListOf<String>()

	init {
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
			if(current.startsWith("-")) {
				val key = current.substring(1)
				if(key.startsWith("-")) {
					val key2 = key.substring(1)
					addNextValue(key2, idx)
				}
				else {
					addNextValue(key, idx)
				}
			}
			else {
				standalone.add(current)
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

}