package command

data class CommandStore(val command: String, val args: Array<String>) {

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other == null || this::class.js != other::class.js) return false

		other as CommandStore

		if (command != other.command) return false
		if (!args.contentEquals(other.args)) return false

		return true
	}

	override fun hashCode(): Int {
		var result = command.hashCode()
		result = 31 * result + args.contentHashCode()
		return result
	}

}