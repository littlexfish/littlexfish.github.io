package command

enum class CommandType {
	COMMON, DEBUG, FS;
	companion object {
		val ALL_NO_DEBUG = entries.toSet().filter { it != DEBUG }
	}
}