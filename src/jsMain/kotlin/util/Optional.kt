package util

class Optional<T> private constructor(private var value: T? = null, private var empty: Boolean = value == null) {

	companion object {
		fun <T> of(value: T): Optional<T> {
			return Optional(value)
		}

		fun <T> ofNullable(value: T?): Optional<T> {
			return Optional(value, false)
		}

		fun <T> empty(): Optional<T> {
			return Optional()
		}
	}

	fun isEmpty(): Boolean {
		return empty
	}

	fun isPresent(): Boolean {
		return value != null && !empty
	}

	fun get(): T {
		return value!!
	}

	fun getOrNull(): T? {
		return value
	}

	fun set(value: T) {
		this.value = value
		empty = false
	}

	fun clear() {
		value = null
		empty = true
	}

	fun orElse(other: T): T {
		return value ?: other
	}

	fun orElseGet(other: () -> T): T {
		return value ?: other()
	}

	fun <X : Throwable> orElseThrow(exceptionSupplier: () -> X): T {
		return value ?: throw exceptionSupplier()
	}

	fun <U> map(mapper: (T) -> U): Optional<U> {
		return if(isPresent()) {
			ofNullable(mapper(value!!))
		}
		else {
			empty()
		}
	}

	fun <U> flatMap(mapper: (T) -> Optional<U>): Optional<U> {
		return if(isPresent()) {
			mapper(value!!)
		}
		else {
			empty()
		}
	}

	override fun equals(other: Any?): Boolean {
		if(this === other) return true
		if(other !is Optional<*>) return false

		if(empty == other.empty) return true
		if(value != other.value) return false

		return true
	}

	override fun hashCode(): Int {
		var result = value?.hashCode() ?: 0
		result = 31 * result + empty.hashCode()
		return result
	}

}