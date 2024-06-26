package com.lint.core.detector

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiTryStatement
import com.intellij.psi.impl.source.tree.java.MethodElement
import com.lint.core.Constants.ANDROID_COLOR
import com.lint.core.Constants.PROJECT_COLOR_UTIL
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UTryExpression
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.kotlin.KotlinUFile


/**
 * 转换规范
 * @author pang
 * @date 2022/11/23 14:52
 */
class ParseDetector : Detector(), Detector.UastScanner {

    companion object {

        private const val COLOR_MESSAGE = "使用Color.parseColor需对异常进行处理"

        @JvmField
        val COLOR_ISSUE = Issue.create(
            "ColorStandard",
            "parseColor使用不规范",
            COLOR_MESSAGE,
            Category.MESSAGES,
            8,
            Severity.ERROR,
            Implementation(ParseDetector::class.java, Scope.JAVA_FILE_SCOPE)
        ).setAndroidSpecific(true)
    }

    override fun getApplicableMethodNames(): List<String>? {
        return listOf("parseColor")
    }

    override fun visitMethodCall(context: JavaContext, node: UCallExpression, method: PsiMethod) {
        if (isWrappedByTryCatch(node, context)) {
            return
        }

        if (context.evaluator.isMemberInClass(method, ANDROID_COLOR)) {
            context.report(
                COLOR_ISSUE, node,
                context.getCallLocation(node, includeReceiver = true, includeArguments = true),
                COLOR_MESSAGE,
                lintFix(context, node)
            )
        }
    }

    /**
     * 是否已经做了 try处理
     */
    private fun isWrappedByTryCatch(node: UCallExpression, context: JavaContext): Boolean {
        if (context.uastFile is KotlinUFile) {
            return node.uastParent?.getParentOfType<UTryExpression>(true) != null
        }
        var parent = node.sourcePsi?.parent
        while (parent != null && parent !is MethodElement) {
            if (parent is PsiTryStatement) {
                return true
            }
            parent = parent.parent
        }
        return false
    }

    private fun lintFix(context: JavaContext, node: UCallExpression): LintFix {
        var importClass: LintFix? = context.importPackage(PROJECT_COLOR_UTIL)

        val fix1 = LintFix.create()
            .name("add try catch")
            .replace()
            .with(
                "try {\n" +
                        "   Color.${node.methodName}(${node.valueArguments[0].asSourceString()})\n" +
                        "} catch (e: IllegalArgumentException) {\n" +
                        "    e.printStackTrace()\n" +
                        "}"
            )
            .build()

        val builder = fix()
            .replace()
            .with("ColorUtil.${node.methodName}(${node.valueArguments[0].asSourceString()})")
            .build()
        val fix2 = fix()
            .name("replace ColorUtil.${node.methodName}(${node.valueArguments[0].asSourceString()})")
            .composite()
            .add(builder).apply {
                if (importClass != null) {
                    add(importClass)
                }
            }
            .build()
        return fix().group(fix1, fix2)
    }

}