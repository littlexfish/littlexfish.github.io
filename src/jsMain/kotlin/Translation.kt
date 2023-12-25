import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import org.w3c.fetch.RequestInit
import kotlin.js.json

object Translation {

	private val i18n = mapOf(
		"en" to "en_US",
		"en-US" to "en_US",
		"zh" to "zh_TW",
		"zh-TW" to "zh_TW",
	)
	private const val DEFAULT_LANG = "en"

	private val translationMapping = HashMap<String, String>()

	private var currentLocale = DEFAULT_LANG

	suspend fun init() {
		val lang = getOverrideLang() ?: getHostLang() ?: DEFAULT_LANG
		loadLang(lang)
		currentLocale = lang
	}

	private fun getHostLang(): String? {
		val hostLang = window.navigator.language
		return if(i18n.containsKey(hostLang)) {
			i18n[hostLang]
		}
		else {
			val lang = hostLang.split("-")[0]
			if(i18n.containsKey(lang)) {
				i18n[lang]
			}
			else null
		}
	}

	private fun getOverrideLang(): String? {
		val overrideLang = URL(window.location.href).searchParams.get("l")
		return if(overrideLang != null && i18n.containsKey(overrideLang)) {
			i18n[overrideLang]
		}
		else {
			val lang = overrideLang?.split("-")?.get(0)
			if(i18n.containsKey(lang)) {
				i18n[lang]
			}
			else null
		}
	}

	private suspend fun loadLang(lang: String) {
		val dLang = i18n[DEFAULT_LANG]!!
		val entries = js("Object.entries")
		val req = RequestInit("GET", headers = json("Accept" to "application/json"))
		if(lang != dLang) {
			val resDef = window.fetch("i18n/${dLang}.json", req).await()
			val jsonDef = resDef.json().await()
			translationMapping.clear()
			entries(jsonDef).iterator().forEach {
				translationMapping[it[0].toString()] = it[1].toString()
			}
		}
		val resLang = window.fetch("i18n/$lang.json", req).await()
		val jsonLang = resLang.json().await()
		entries(jsonLang).iterator().forEach {
			translationMapping[it[0].toString()] = it[1].toString()
		}
	}

	operator fun get(key: String): String {
		return translationMapping[key] ?: key
	}

	operator fun get(key: String, vararg args: Pair<String, Any?>): String {
		return this[key, mapOf(*args)]
	}

	operator fun get(key: String, args: Map<String, Any?>): String {
		return translationMapping[key]?.format(args) ?: key
	}

	fun getCurrentLocale(): String {
		return currentLocale.replace("_", "-")
	}

}