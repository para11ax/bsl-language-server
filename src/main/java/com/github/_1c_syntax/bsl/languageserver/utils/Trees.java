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
package com.github._1c_syntax.bsl.languageserver.utils;

import com.github._1c_syntax.bsl.parser.BSLParser;
import com.github._1c_syntax.bsl.parser.BSLParserRuleContext;
import lombok.experimental.UtilityClass;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public final class Trees {

  private static final Set<Integer> VALID_TOKEN_TYPES_FOR_COMMENTS_SEARCH = Set.of(
    BSLParser.ANNOTATION_ATCLIENT_SYMBOL,
    BSLParser.ANNOTATION_ATSERVERNOCONTEXT_SYMBOL,
    BSLParser.ANNOTATION_ATCLIENTATSERVERNOCONTEXT_SYMBOL,
    BSLParser.ANNOTATION_ATCLIENTATSERVER_SYMBOL,
    BSLParser.ANNOTATION_ATSERVER_SYMBOL,
    BSLParser.ANNOTATION_CUSTOM_SYMBOL,
    BSLParser.ANNOTATION_UKNOWN,
    BSLParser.LINE_COMMENT,
    BSLParser.WHITE_SPACE,
    BSLParser.RULE_annotationParams
  );

  /**
   * Обертки Trees
   */

  public static Collection<ParseTree> findAllRuleNodes(ParseTree t, int ruleIndex) {
    return org.antlr.v4.runtime.tree.Trees.findAllRuleNodes(t, ruleIndex);
  }

  public static List<Tree> getChildren(Tree t) {
    return org.antlr.v4.runtime.tree.Trees.getChildren(t);
  }

  public static Collection<ParseTree> findAllTokenNodes(ParseTree t, int ttype) {
    return org.antlr.v4.runtime.tree.Trees.findAllTokenNodes(t, ttype);
  }

  public static List<ParseTree> getDescendants(ParseTree t) {
    return org.antlr.v4.runtime.tree.Trees.getDescendants(t);
  }

  /**
   * Собственная реализация
   */

  private static int getRuleIndex(ParseTree node) {
    if (node instanceof TerminalNode) {
      return ((TerminalNode) node).getSymbol().getType();
    } else {
      return ((BSLParserRuleContext) node).getRuleIndex();
    }
  }

  private static List<ParseTree> getDescendantsWithFilter(ParseTree parent, ParseTree tnc, int ruleindex) {
    List<ParseTree> descendants;
    if (getRuleIndex(tnc) == ruleindex) {
      descendants = new ArrayList<>(org.antlr.v4.runtime.tree.Trees.findAllRuleNodes(parent, ruleindex));
    } else {
      descendants = org.antlr.v4.runtime.tree.Trees.getDescendants(parent)
        .stream()
        .filter(node -> node instanceof BSLParserRuleContext)
        .filter(node -> (node.equals(tnc)
          || getRuleIndex(node) == ruleindex))
        .collect(Collectors.toList());
    }
    return descendants;
  }

  /**
   * Ищем предка элемента по указанному типу BSLParser
   * Пример:
   * ParserRuleContext parent = Trees.getAncestorByRuleIndex(ctx, BSLParser.RULE_statement);
   */
  @CheckForNull
  public static ParserRuleContext getAncestorByRuleIndex(ParserRuleContext element, int type) {
    ParserRuleContext parent = element.getParent();
    if (parent == null) {
      return null;
    }
    if (parent.getRuleIndex() == type) {
      return parent;
    }
    return getAncestorByRuleIndex(parent, type);
  }

  /**
   * Проверяет среди всех дочерних элементов (рекурсивно) наличие узла с ошибкой
   *
   * @return true - если есть узел с ошибкой
   */
  public static boolean treeContainsErrors(ParseTree tnc) {
    return treeContainsErrors(tnc, true);
  }

  /**
   * Проверяет среди дочерних элементов узла наличие узла с ошибкой
   *
   * @return true - если есть узел с ошибкой
   */
  public static boolean nodeContainsErrors(ParseTree tnc) {
    return treeContainsErrors(tnc, false);
  }

  /**
   * Выполняет поиск предыдущей ноды нужного типа
   *
   * @param parent    - родительская нода, среди дочерних которой производится поиск
   * @param tnc       - нода, для которой ищем предыдущую
   * @param ruleindex - BSLParser.RULE_*
   * @return tnc - если предыдущая нода не найдена, вернет текущую
   */
  public static ParseTree getPreviousNode(ParseTree parent, ParseTree tnc, int ruleindex) {

    List<ParseTree> descendants = getDescendantsWithFilter(parent, tnc, ruleindex);

    int pos = descendants.indexOf(tnc);
    if (pos > 0) {
      return descendants.get(pos - 1);
    }

    return tnc;
  }

  /**
   * @param tokens     - полный список токенов (см. {@link com.github._1c_syntax.bsl.languageserver.context.DocumentContext#getTokens()}
   * @param tokenIndex - индекс текущего токена в переданном списке токенов
   * @return предыдущий токен, если он был найден
   */
  public static Optional<Token> getPreviousTokenFromDefaultChannel(List<Token> tokens, int tokenIndex) {
    while (true) {
      if (tokenIndex == 0) {
        return Optional.empty();
      }
      Token token = tokens.get(tokenIndex);
      if (token.getChannel() != Token.DEFAULT_CHANNEL) {
        tokenIndex = tokenIndex - 1;
        continue;
      }

      return Optional.of(token);
    }
  }

  /**
   * Выполняет поиск следующей ноды нужного типа
   *
   * @param parent    - родительская нода, среди дочерних которой производится поиск
   * @param tnc       - нода, для которой ищем следующую
   * @param ruleindex - BSLParser.RULE_*
   * @return tnc - если следующая нода не найдена, вернет текущую
   */
  public static ParseTree getNextNode(ParseTree parent, ParseTree tnc, int ruleindex) {

    List<ParseTree> descendants = getDescendantsWithFilter(parent, tnc, ruleindex);

    int pos = descendants.indexOf(tnc);
    if (pos + 1 < descendants.size()) {
      return descendants.get(pos + 1);
    }

    return tnc;
  }

  /**
   * Рекурсивно находит самого верхнего родителя текущей ноды
   */
  public static BSLParserRuleContext getRootParent(BSLParserRuleContext tnc) {
    if (tnc.getParent() != null) {
      return getRootParent((BSLParserRuleContext) tnc.getParent());
    }

    return tnc;
  }

  /**
   * Рекурсивно находит самого верхнего родителя текущей ноды нужно типа
   */
  public static BSLParserRuleContext getRootParent(BSLParserRuleContext tnc, int ruleindex) {
    if (tnc.getParent() == null) {
      return null;
    }

    if (getRuleIndex(tnc.getParent()) == ruleindex) {
      return (BSLParserRuleContext) tnc.getParent();
    } else {
      return getRootParent((BSLParserRuleContext) tnc.getParent(), ruleindex);
    }
  }

  /**
   * Получает детей с нужными типами
   */
  public static List<BSLParserRuleContext> getChildren(Tree t, Integer... ruleIndex) {
    List<Integer> indexes = Arrays.asList(ruleIndex);
    return IntStream.range(0, t.getChildCount())
      .mapToObj(t::getChild)
      .filter((Tree child) ->
        child instanceof BSLParserRuleContext
          && indexes.contains(((BSLParserRuleContext) child).getRuleIndex()))
      .map(child -> (BSLParserRuleContext) child)
      .collect(Collectors.toList());
  }

  /**
   * Получает дочерние ноды с нужными типами
   */
  public static Collection<ParserRuleContext> findAllRuleNodes(ParseTree t, Integer... index) {
    List<ParserRuleContext> nodes = new ArrayList<>();
    List<Integer> indexes = Arrays.asList(index);

    if (t instanceof ParserRuleContext
      && indexes.contains(((ParserRuleContext) t).getRuleIndex())) {
      nodes.add((ParserRuleContext) t);
    }

    IntStream.range(0, t.getChildCount())
      .mapToObj(i -> findAllRuleNodes(t.getChild(i), index))
      .forEachOrdered(nodes::addAll);

    return nodes;
  }

  /**
   * Проверяет наличие дочерней ноды с указанным типом
   */
  public static boolean nodeContains(ParseTree t, Integer... index) {
    Set<Integer> indexes = new HashSet<>(Arrays.asList(index));

    if (t instanceof ParserRuleContext
      && indexes.contains(((ParserRuleContext) t).getRuleIndex())) {
      return true;
    }

    return IntStream.range(0, t.getChildCount())
      .anyMatch(i -> nodeContains(t.getChild(i), index));
  }

  /**
   * Проверяет наличие дочерней ноды с указанным типом исключая переданную
   */
  public static boolean nodeContains(ParseTree t, ParseTree exclude, Integer... index) {
    Set<Integer> indexes = new HashSet<>(Arrays.asList(index));

    if (t instanceof ParserRuleContext
      && !t.equals(exclude)
      && indexes.contains(((ParserRuleContext) t).getRuleIndex())) {
      return true;
    }

    return IntStream.range(0, t.getChildCount())
      .anyMatch(i -> nodeContains(t.getChild(i), exclude, index));
  }

  /**
   * @param tokens - список токенов из DocumentContext
   * @param token - токен, на строке которого требуется найти висячий комментарий
   * @return - токен с комментарием, если он найден
   */
  public static Optional<Token> getTrailingComment(List<Token> tokens, Token token) {
    int index = token.getTokenIndex();
    int size = tokens.size();
    int currentIndex = index + 1;
    int line = token.getLine();

    while (currentIndex < size) {
      var nextToken = tokens.get(currentIndex);
      if (nextToken.getLine() > line) {
        break;
      }
      if (nextToken.getType() == BSLParser.LINE_COMMENT) {
        return Optional.of(nextToken);
      }
      currentIndex++;
    }

    return Optional.empty();

  }

  /**
   * Поиск комментариев назад от указанного токена
   *
   * @param tokens - список токенов DocumentContext
   * @param token  - токен, для которого требуется найти комментарии
   * @return - список найденных комментариев lines
   */
  public static List<Token> getComments(List<Token> tokens, Token token) {
    List<Token> comments = new ArrayList<>();
    fillCommentsCollection(tokens, token, comments);
    return comments;
  }

  private static void fillCommentsCollection(List<Token> tokens, Token currentToken, List<Token> lines) {

    int index = currentToken.getTokenIndex();

    if (index == 0) {
      return;
    }

    Token previousToken = tokens.get(index - 1);

    if (abortSearchComments(previousToken, currentToken)) {
      return;
    }

    fillCommentsCollection(tokens, previousToken, lines);
    int type = previousToken.getType();
    if (type == BSLParser.LINE_COMMENT) {
      lines.add(previousToken);
    }
  }

  private static boolean abortSearchComments(Token previousToken, Token currentToken) {
    int type = previousToken.getType();
    return !VALID_TOKEN_TYPES_FOR_COMMENTS_SEARCH.contains(type) || isBlankLine(previousToken, currentToken);
  }

  private static boolean isBlankLine(Token previousToken, Token currentToken) {
    return previousToken.getType() == BSLParser.WHITE_SPACE
      && (previousToken.getTokenIndex() == 0
      || (previousToken.getLine() + 1) != currentToken.getLine());
  }


  private static boolean treeContainsErrors(ParseTree tnc, boolean recursive) {
    if (!(tnc instanceof BSLParserRuleContext)) {
      return false;
    }

    BSLParserRuleContext ruleContext = (BSLParserRuleContext) tnc;

    if (ruleContext.exception != null) {
      return true;
    }

    return recursive
      && ruleContext.children != null
      && ruleContext.children.stream().anyMatch(Trees::treeContainsErrors);
  }
}
