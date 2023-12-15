package ext

@JsModule("DOMException")
@JsNonModule
external class DOMException : Throwable {

	override val message: String
	val name: String

}

object DOMExceptionName {

	const val INDEX_SIZE_ERR = "IndexSizeError"
	const val HIERARCHY_REQUEST_ERR = "HierarchyRequestError"
	const val WRONG_DOCUMENT_ERR = "WrongDocumentError"
	const val INVALID_CHARACTER_ERR = "InvalidCharacterError"
	const val NO_MODIFICATION_ALLOWED_ERR = "NoModificationAllowedError"
	const val NOT_FOUND_ERR = "NotFoundError"
	const val NOT_SUPPORTED_ERR = "NotSupportedError"
	const val INVALID_STATE_ERR = "InvalidStateError"
	const val INUSE_ATTRIBUTE_ERR = "InUseAttributeError"
	const val SYNTAX_ERR = "SyntaxError"
	const val INVALID_MODIFICATION_ERR = "InvalidModificationError"
	const val NAMESPACE_ERR = "NamespaceError"
	const val INVALID_ACCESS_ERR = "InvalidAccessError"

	/**
	 * @deprecated the JavaScript TypeError exception is now raised instead of a DOMException with this value.
	 */
	@Deprecated("Use TypeError instead")
	const val TYPE_MISMATCH_ERR = "TypeMismatchError"
	const val SECURITY_ERR = "SecurityError"
	const val NETWORK_ERR = "NetworkError"
	const val ABORT_ERR = "AbortError"
	const val URL_MISMATCH_ERR = "URLMismatchError"
	const val QUOTA_EXCEEDED_ERR = "QuotaExceededError"
	const val TIMEOUT_ERR = "TimeoutError"
	const val INVALID_NODE_TYPE_ERR = "InvalidNodeTypeError"
	const val DATA_CLONE_ERR = "DataCloneError"

}
