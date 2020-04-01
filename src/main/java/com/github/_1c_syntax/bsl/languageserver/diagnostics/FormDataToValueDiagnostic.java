/*
 * This file is a part of BSL Language Server.
 *
 * Copyright © 2018-2020
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticInfo;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticScope;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.utils.Trees;
import com.github._1c_syntax.bsl.parser.BSLLexer;
import com.github._1c_syntax.bsl.parser.BSLParser;
import com.github._1c_syntax.bsl.parser.BSLParser.GlobalMethodCallContext;

import java.util.List;
import java.util.regex.Pattern;

@DiagnosticMetadata(
  type = DiagnosticType.CODE_SMELL,
  severity = DiagnosticSeverity.INFO,
  scope = DiagnosticScope.BSL,
  minutesToFix = 5,
  tags = {
    DiagnosticTag.BADPRACTICE
  }

)
public class FormDataToValueDiagnostic extends AbstractFindMethodDiagnostic {

  private static final Pattern MESSAGE_PATTERN = Pattern.compile(
    "ДанныеФормыВЗначение|FormDataToValue",
    Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
  );

  public FormDataToValueDiagnostic(DiagnosticInfo info) {
    super(info, MESSAGE_PATTERN);
  }

  @Override
  protected boolean checkGlobalMethodCall(GlobalMethodCallContext ctx) {
    var parentNode = (BSLParser.SubContext) Trees.getAncestorByRuleIndex(ctx, BSLParser.RULE_sub);

    if (parentNode == null) {
      return false;
    }

    List<? extends BSLParser.CompilerDirectiveContext> compileList;

    if (parentNode.procedure() == null) {
      compileList = parentNode.function().funcDeclaration().compilerDirective();
    } else {
      compileList = parentNode.procedure().procDeclaration().compilerDirective();
    }

    if (compileList.isEmpty()
      || (compileList.get(0).getStop().getType() != BSLLexer.ANNOTATION_ATSERVERNOCONTEXT_SYMBOL
      && compileList.get(0).getStop().getType() != BSLLexer.ANNOTATION_ATCLIENTATSERVERNOCONTEXT_SYMBOL)) {

      return MESSAGE_PATTERN.matcher(ctx.methodName().getText()).matches();
    }

    return false;
  }

}
