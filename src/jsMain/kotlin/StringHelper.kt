

fun String.format(map: Map<String, Any?>): String {
	val regex = "\\{\\s*[a-zA-Z0-9_]+\\s*\\}".toRegex()
	val sb = StringBuilder()
	var lastEnd = 0
	while(lastEnd < this.length) {
		val match = regex.find(this, lastEnd)
		if(match == null) {
			sb.append(this.substring(lastEnd))
			break
		}
		else {
			sb.append(this.substring(lastEnd, match.range.first))
			val key = match.value.substring(1, match.value.length - 1).trim()
			sb.append(map[key] ?: match.value)
			lastEnd = match.range.last + 1
		}
	}
	return sb.toString()
}