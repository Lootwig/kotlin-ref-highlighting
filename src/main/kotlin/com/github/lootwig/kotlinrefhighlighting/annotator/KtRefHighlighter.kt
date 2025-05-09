/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2023 Elior "Mallowigi" Boukhobza
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 *
 */

package com.github.lootwig.kotlinrefhighlighting.annotator

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import org.jetbrains.kotlin.idea.highlighter.KotlinHighlightingColors.FUNCTION_CALL
import org.jetbrains.kotlin.psi.KtCallableReferenceExpression

internal class KtRefHighlighter : ExternalAnnotator<List<TextRange>, List<TextRange>>(), DumbAware {
  internal class RefVisitor : PsiRecursiveElementVisitor() {
    var offset = 0
    val refRanges = arrayListOf<TextRange>()

    override fun visitElement(element: PsiElement) {
      element.apply {
        val startOffset = startOffsetInParent
        offset += startOffset
        try {
          when (this) {
            is KtCallableReferenceExpression -> refRanges.add(callableReference.textRangeInParent.shiftRight(offset))
            else -> super.visitElement(element)
          }
        } finally {
          offset -= startOffset
        }
      }
    }
  }

  override fun collectInformation(file: PsiFile) = RefVisitor().apply { visitFile(file) }.refRanges

  override fun doAnnotate(collectedInfo: List<TextRange>) = collectedInfo

  override fun apply(file: PsiFile, ranges: List<TextRange>, holder: AnnotationHolder) =
    ranges.forEach(holder::markFunctionCall)
}

internal fun AnnotationHolder.markFunctionCall(range: TextRange) =
  newSilentAnnotation(HighlightSeverity.INFORMATION).textAttributes(FUNCTION_CALL).range(range).create()

