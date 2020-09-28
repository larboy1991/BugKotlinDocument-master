package com.github.zxj5470.bugktdoc.options

import com.github.zxj5470.bugktdoc.globalSettings
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StorageScheme
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * @author zxj5470
 * @date 2018/4/2
 */

data class BugKtDocSettings(
	var useBugKtDoc: Boolean = true,
	var theFirstTile: Boolean = true,
	var alwaysShowUnitReturnType: Boolean = false,
	var alwaysShowClassFieldProperty: Boolean = false,
	var alwaysShowConstructor: Boolean = true,
	var alwaysShowAuthor: Boolean = true,
	var author: String = "",
	var alwaysShowCompany: Boolean = true,
	var company: String = ""
)

interface BugKtDocGlobalSettings {
	val settings: BugKtDocSettings
}

/**
 * @ref julia-intellij
 */
@State(
	name = "BugKtDocSettings",
	storages = [Storage(file = "BugKtDocSettings.xml")])
class BugKtDocGlobalSettingsImpl :
	BugKtDocGlobalSettings, PersistentStateComponent<BugKtDocSettings> {

	override val settings = BugKtDocSettings(true)
	override fun getState(): BugKtDocSettings? {
		return XmlSerializerUtil.createCopy(settings)
	}


	override fun loadState(state: BugKtDocSettings) {
		XmlSerializerUtil.copyBean(state, settings)
	}

}