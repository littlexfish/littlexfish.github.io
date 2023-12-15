package fs

class NotFoundException : Exception()
class TypeNotMatchException(isDir: Boolean) : Exception("Not a ${if(isDir) "directory" else "file"}")
