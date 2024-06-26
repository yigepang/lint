package com.lint.core.detector

import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.lint.core.Constants.ANDROID_ADAPTER
import com.lint.core.Constants.ANDROID_FRAGMENT_STATE_ADAPTER
import com.lint.core.Constants.PROJECT_BANNER_ADAPTER
import com.lint.core.Constants.PROJECT_GROUPED_RECYCLER_VIEW_ADAPTER
import com.lint.core.Constants.PROJECT_MULTI_ADAPTER
import com.lint.core.Constants.PROJECT_SINGLE_ADAPTER
import org.jetbrains.uast.UClass

/**
 * 适配器规范
 * @author pang
 * @date 2022/11/21 14:19
 */
class AdapterDetector : Detector(), SourceCodeScanner {


    companion object {

        private const val MESSAGE = "Adapter应继承自【${PROJECT_SINGLE_ADAPTER}】或【${PROJECT_MULTI_ADAPTER}】或其子类"

        @JvmField
        val ISSUE = Issue.create(
            "AdapterStandard",
            "Adapter继承不规范",
            MESSAGE,
            Category.MESSAGES,
            7,
            Severity.FATAL,
            Implementation(AdapterDetector::class.java, Scope.JAVA_FILE_SCOPE)
        )
    }

    override fun applicableSuperClasses(): List<String>? {
        return listOf(ANDROID_ADAPTER)
    }

    override fun visitClass(context: JavaContext, declaration: UClass) {
        val evaluator = context.evaluator
        //排除FragmentStateAdapter
        if (evaluator.extendsClass(declaration.javaPsi,ANDROID_FRAGMENT_STATE_ADAPTER,true)){
            return
        }
        //排除GroupedRecyclerViewAdapter
        if (evaluator.extendsClass(declaration.javaPsi,PROJECT_GROUPED_RECYCLER_VIEW_ADAPTER,true)){
            return
        }
        //排除GroupedRecyclerViewAdapter
        if (evaluator.extendsClass(declaration.javaPsi,PROJECT_BANNER_ADAPTER,true)){
            return
        }
        //判断是否继承指定类
        if (!evaluator.extendsClass(declaration.javaPsi, PROJECT_SINGLE_ADAPTER, true) &&
            !evaluator.extendsClass(declaration.javaPsi, PROJECT_MULTI_ADAPTER, true)
        ) {
            context.report(ISSUE, declaration, context.getNameLocation(declaration), MESSAGE)
        }
    }
}