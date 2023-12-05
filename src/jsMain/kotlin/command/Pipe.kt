package command

class Pipe(val current: CommandStore, val type: PipeType, private var next: Pipe?) {

	fun addPipe(n: CommandStore, type: PipeType) {
		if(next == null) {
			next = Pipe(n, type, null)
		}
		else {
			next!!.addPipe(n, type)
		}
	}

	fun getNext(): Pipe? {
		return next
	}

}

enum class PipeType(val str: String) {
	PIPE_NEXT("|"), EXECUTE(";"), SUC_NEXT("&&"), FAIL_NEXT("||"), NONE("")
}