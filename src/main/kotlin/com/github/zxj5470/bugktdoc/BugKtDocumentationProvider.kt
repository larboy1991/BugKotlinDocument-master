package com.github.zxj5470.bugktdoc

import com.github.zxj5470.bugktdoc.constants.*
import com.github.zxj5470.bugktdoc.options.BugKtDocSettings
import com.intellij.codeInsight.editorActions.CodeDocumentationUtil
import com.intellij.ide.util.PackageUtil
import com.intellij.lang.CodeDocumentationAwareCommenter
import com.intellij.lang.LanguageCommenters
import com.intellij.lang.documentation.CodeDocumentationProvider
import com.intellij.lang.documentation.DocumentationProviderEx
import com.intellij.lang.java.JavaDocumentationProvider.getPackageInfoComment
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.containers.isNullOrEmpty
import org.jetbrains.kotlin.kdoc.psi.impl.KDocImpl
import org.jetbrains.kotlin.psi.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author zxj5470
 * @date 2018/4/6
 */
class BugKtDocumentationProvider : DocumentationProviderEx(), CodeDocumentationProvider {
	 /**
	  *
	  * @param startPoint PsiElement
	  * @return Pair<PsiElement, PsiComment>?
	  */
	 override fun parseContext(startPoint: PsiElement): Pair<PsiElement, PsiComment>? {
		  var current = startPoint
		  while (true) {
				if (current is KDocImpl) {
					 return Pair.create(if (current is PsiField) current.modifierList else current, current)
				} else if (PackageUtil.isPackageInfoFile(current)) {
					 return Pair.create(current, getPackageInfoComment(current))
				}
				current = current.parent
		  }
	 }

	 private fun PsiComment.getOwner() = PsiTreeUtil.getParentOfType<KtDeclaration>(this)
				?: this.parent.takeIf { it is KtFunction || it is KtClass || it is KtSecondaryConstructor || it is KtObjectDeclaration}


	 /**
	  *Desc:
	  * <p>
	  * author:
	  * Date: 2019-09-30
	  * @param contextComment PsiComment
	  * @return String?
	  */
	 override fun generateDocumentationContentStub(contextComment: PsiComment?): String? {
		  if (!pluginActive || contextComment == null) return ""
		  val owner = contextComment.getOwner()

		  val commenter = LanguageCommenters.INSTANCE.forLanguage(contextComment.language) as CodeDocumentationAwareCommenter

		  fun StringBuilder.appendDecorate(str: String) = append(CodeDocumentationUtil.createDocCommentLine(str, contextComment.containingFile, commenter))

		  fun docKtNamedFunction(owner: KtNamedFunction) = buildString {
				appendDecorate(PARAGRAPH)
				append(LF)
				if (isAlwaysShowAuthor) {
					 appendDecorate(AUTHOR)
					 append(author)
					 append(LF)
				}
				appendDecorate(DATE)
				append(formatTime())
				append(LF)

				// @receiver
				owner.receiverTypeReference?.let {
					 appendDecorate(RECEIVER)
					 append(it.text)
					 append(LF)
				}

				// @param
				owner.valueParameters.forEach {
					 val param = it.nameIdentifier?.text ?: ""
					 val type = it.itsType
					 appendDecorate(PARAM)
					 // add a space before `param` and after is no used
					 append("$param $type")
					 append(LF)
				}


				// @return
				if (owner.hasDeclaredReturnType()) {
					 appendDecorate(RETURN)
					 append(owner.typeReference?.typeElement?.text)
					 append(LF)
				} else {
					 owner.itsType.let {
						  if (isAlwaysShowUnitReturnType || it != "Unit") {
								appendDecorate(RETURN)
								append(it)
								append(LF)
						  }
					 }
				}

				// @throws
				PsiTreeUtil.findChildrenOfType(owner, KtAnnotationEntry::class.java)
						  .firstOrNull { it.calleeExpression?.text == "Throws" }
						  ?.valueArguments?.forEach {
								(it.getArgumentExpression() as? KtClassLiteralExpression)?.let {
									 PsiTreeUtil.findChildOfType(it, KtNameReferenceExpression::class.java)?.text?.let {
										  appendDecorate(THROWS)
										  append(it)
										  append(LF)
									 }
								}
						  }
		  }

		  fun docKtObjectClass(): String {
				return buildString {
					 appendDecorate(PARAGRAPH)
					 append(LF)
					 appendDecorate(DATE)
					 append(formatTime())
					 append(LF)
					 if (isAlwaysCompany) {
						  appendDecorate(COMPANY)
						  append(company)
						  append(LF)
					 }
					 appendDecorate(UPDATER)
					 append(LF)
					 appendDecorate(UPDATE_TIME)
					 append(LF)
					 appendDecorate(UPDATE_COMMENTS)
					 append(LF)

					 if (isAlwaysShowAuthor) {
						  appendDecorate("")
						  append(LF)
						  appendDecorate(AUTHOR)
						  append(author)
						  append(LF)
					 }

				}
		  }

		  fun docKtClass(owner: KtClass): String {
				return buildString {
					 appendDecorate(PARAGRAPH)
					 append(LF)
					 appendDecorate(DATE)
					 append(formatTime())
					 append(LF)
					 if (isAlwaysCompany) {
						  appendDecorate(COMPANY)
						  append(company)
						  append(LF)
					 }
					 appendDecorate(UPDATER)
					 append(LF)
					 appendDecorate(UPDATE_TIME)
					 append(LF)
					 appendDecorate(UPDATE_COMMENTS)
					 append(LF)
					 if (owner.typeParameters.size > 0) {
						  appendDecorate("")
						  append(LF)
					 }
					 owner.typeParameters.forEach {
						  appendDecorate(PARAM)
						  append(it.text)
						  append(LF)
					 }

					 // order: 1. primary Parameters -> @property
					 owner.primaryConstructorParameters.forEach {
						  // is property
						  if (it.hasValOrVar()) {
								val param = it.nameIdentifier?.text
								val type = it.itsType
								if (!param.isNullOrEmpty() && !type.isEmpty()) {
									 appendDecorate(PARAM)
									 // add a space before or after is no used
									 append("$param $type")
									 append(LF)
								}
						  }
					 }

					 // order: 2. class fields -> @property
					 if (isAlwaysShowClassFieldProperty)
						  owner.getProperties().forEach {
								val param = it.nameIdentifier?.text
								val type = it.itsType
								if (!param.isNullOrEmpty()) {
									 appendDecorate(PROPERTY)
									 // add a space before or after is no used
									 append("$param $type")
									 append(LF)
								}
						  }


					 // @constructor
					 if (owner.hasPrimaryConstructor() && isAlwaysShowConstructor) {
						  // empty class
						  if (!owner.getPrimaryConstructorParameterList()?.parameters.isNullOrEmpty()) {
								appendDecorate(CONSTRUCTOR)
								append(LF)
						  }
					 }
					 if (isAlwaysShowAuthor) {
						  appendDecorate("")
						  append(LF)
						  appendDecorate(AUTHOR)
						  append(author)
						  append(LF)
					 }

				}
		  }


		  fun docKtConstructor(owner: KtConstructor<*>): String {
				return buildString {

					 // @param
					 owner.getValueParameters().forEach {
						  val param = it.nameIdentifier?.text
						  val type = it.itsType
						  if (!param.isNullOrEmpty() && !type.isEmpty()) {
								appendDecorate(PARAM)
								append("$param $type")
								append(LF)
						  }
					 }

					 // @constructor
					 if (isAlwaysShowConstructor) {
						  appendDecorate(CONSTRUCTOR)
						  append(LF)
					 }
				}
		  }

		  return when (owner) {
				is KtNamedFunction -> docKtNamedFunction(owner)
				is KtClass -> docKtClass(owner)
				is KtObjectDeclaration-> docKtObjectClass()
				is KtConstructor<*> -> docKtConstructor(owner)
				else -> ""
		  }

	 }

	 private fun formatTime(): String {
		  return SimpleDateFormat("yyyy-MM-dd").format(Date())
	 }

	 override fun findExistingDocComment(contextElement: PsiComment?): PsiComment? =
				if (!isKotlinNative) (contextElement as? KDocImpl)?.getOwner()?.docComment else contextElement
}