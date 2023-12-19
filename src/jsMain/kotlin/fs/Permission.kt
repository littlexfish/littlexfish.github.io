package fs

data class Permission internal constructor(val read: Boolean, val write: Boolean) {
	companion object {
		val DEFAULT = Permission(read = false, write = false)
		val READ_ONLY = Permission(read = true, write = false)
		val WRITE_ONLY = Permission(read = false, write = true)
		val ALL = Permission(read = true, write = true)
	}

	override fun toString(): String {
		return (if(read) "r" else "-") + (if(write) "w" else "-")
	}
}
