package com.github.zxj5470.bugktdoc.options

import com.github.zxj5470.bugktdoc.BugKtDocBundle
import com.github.zxj5470.bugktdoc.globalSettings
import com.intellij.ui.layout.jbTextField
import javax.swing.JCheckBox
import javax.swing.JPanel
import javax.swing.JTextField

/**
 * @author zxj5470
 * @date 2018/4/2
 */
class BugKtDocConfigureFormImpl : BugKtDocConfigureForm() {
	 private val thisPanel: JPanel
		  get() {
				// what the hell it is in CLion?
				// mainPanel is null in CLion.
				if (mainPanel == null) {
					 mainPanel = JPanel()
					 useBugKtDoc = JCheckBox(BugKtDocBundle.message("bugktdoc.options.use"))
								.apply { mainPanel.add(this) }
					 showUnitTypeDefault = JCheckBox(BugKtDocBundle.message("bugktdoc.options.default.unit"))
								.apply { mainPanel.add(this) }
					 showClassFieldProperty = JCheckBox(BugKtDocBundle.message("bugktdoc.options.default.property"))
								.apply { mainPanel.add(this) }
					 showConstructor = JCheckBox(BugKtDocBundle.message("bugktdoc.options.default.constructor"))
								.apply { mainPanel.add(this) }
					 showAuthor = JCheckBox(BugKtDocBundle.message("bugktdoc.options.default.author"))
								.apply { mainPanel.add(this) }
					 authorEdit = JTextField().apply { mainPanel.add(this) }
					 showCompany = JCheckBox(BugKtDocBundle.message("bugktdoc.options.default.company"))
								.apply { mainPanel.add(this) }
					 companyEdit = JTextField().apply { mainPanel.add(this) }
				}
				useBugKtDoc.isSelected = globalSettings.useBugKtDoc
				showUnitTypeDefault.isSelected = globalSettings.alwaysShowUnitReturnType
				showClassFieldProperty.isSelected = globalSettings.alwaysShowClassFieldProperty
				showConstructor.isSelected = globalSettings.alwaysShowConstructor
				showAuthor.isSelected = globalSettings.alwaysShowAuthor
				authorEdit.text = globalSettings.author
				showCompany.isSelected = globalSettings.alwaysShowAuthor
				companyEdit.text = globalSettings.company
				return mainPanel
		  }

	 init {
		  thisPanel
		  addSwitchListener()
		  observer()
	 }

	 private fun addSwitchListener() {
		  useBugKtDoc?.addActionListener {
				observer()
		  }
	 }

	 private fun observer() {
		  useBugKtDoc?.apply {
				if (this.isSelected) {
					 showUnitTypeDefault.isEnabled = true
					 showClassFieldProperty.isEnabled = true
					 showConstructor.isEnabled = true
					 showAuthor.isEnabled = true
					 showCompany.isEnabled = true
				} else {
					 showUnitTypeDefault.isEnabled = false
					 showClassFieldProperty.isEnabled = false
					 showConstructor.isEnabled = false
					 showAuthor.isEnabled = false
					 showCompany.isEnabled = false
				}
		  }
	 }

	 override fun isModified(): Boolean {
		  return true
	 }

	 /**
	  *
	  * Desc:
	  * <p>
	  * author:
	  * Date: 2019-09-29
	  */
	 override fun reset() {
		  globalSettings.useBugKtDoc = true
		  globalSettings.alwaysShowUnitReturnType = false
		  globalSettings.alwaysShowClassFieldProperty = true
		  globalSettings.alwaysShowConstructor = true
		  globalSettings.alwaysShowAuthor = true
		  globalSettings.author = ""
		  globalSettings.alwaysShowCompany = true
		  globalSettings.company = ""
		  observer()
	 }

	 override fun getDisplayName() = BugKtDocBundle.message("bugktdoc.settings.title")

	 override fun apply() {
		  globalSettings.useBugKtDoc = useBugKtDoc.isSelected
		  globalSettings.alwaysShowUnitReturnType = showUnitTypeDefault.isSelected
		  globalSettings.alwaysShowClassFieldProperty = showClassFieldProperty.isSelected
		  globalSettings.alwaysShowConstructor = showConstructor.isSelected
		  globalSettings.alwaysShowAuthor = showAuthor.isSelected
		  globalSettings.author = authorEdit.text
		  globalSettings.alwaysShowCompany = showCompany.isSelected
		  globalSettings.company = companyEdit.text
	 }

	 override fun createComponent() = thisPanel
}