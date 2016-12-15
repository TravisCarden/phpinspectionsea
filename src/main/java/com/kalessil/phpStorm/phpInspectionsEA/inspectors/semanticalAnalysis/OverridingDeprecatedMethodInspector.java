package com.kalessil.phpStorm.phpInspectionsEA.inspectors.semanticalAnalysis;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.jetbrains.php.lang.psi.elements.Method;
import com.jetbrains.php.lang.psi.elements.PhpClass;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpElementVisitor;
import com.kalessil.phpStorm.phpInspectionsEA.openApi.BasePhpInspection;
import com.kalessil.phpStorm.phpInspectionsEA.utils.FileSystemUtil;
import com.kalessil.phpStorm.phpInspectionsEA.utils.NamedElementUtil;
import org.jetbrains.annotations.NotNull;

/*
 * This file is part of the Php Inspections (EA Extended) package.
 *
 * (c) Vladimir Reznichenko <kalessil@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

public class OverridingDeprecatedMethodInspector extends BasePhpInspection {
    private static final String messagePattern = "%m% overrides/implements a deprecated method. Consider refactoring or deprecate it as well.";

    @NotNull
    public String getShortName() {
        return "OverridingDeprecatedMethodInspection";
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new BasePhpElementVisitor() {
            public void visitPhpMethod(Method method) {
                /* do not process already deprecated methods */
                if (method.isDeprecated()) {
                    return;
                }
                /* do not process un-reportable, test classes and interfaces - we are searching real tech. debt here */
                final PhpClass clazz        = method.getContainingClass();
                final PsiElement methodName = NamedElementUtil.getNameIdentifier(method);
                if (null == methodName || null == clazz|| FileSystemUtil.isTestClass(clazz) || clazz.isInterface()) {
                    return;
                }

                final PhpClass parent     = clazz.getSuperClass();
                final Method parentMethod = null == parent ? null : parent.findMethodByName(method.getName());
                if (null != parentMethod && parentMethod.isDeprecated()) {
                    final String message = messagePattern.replace("%m%", method.getName());
                    holder.registerProblem(methodName, message, ProblemHighlightType.LIKE_DEPRECATED);

                    return;
                }


                /* 2: if super class has no method, lookup interfaces; */
                /* 2: lookup all interfaces for a deprecated method */

            }
        };
    }
}
