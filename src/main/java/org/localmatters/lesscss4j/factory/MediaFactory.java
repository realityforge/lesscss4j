/*
   Copyright 2010-present Local Matters, Inc.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package org.localmatters.lesscss4j.factory;

import org.antlr.runtime.tree.Tree;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.model.Media;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.expression.Expression;

import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class MediaFactory extends AbstractObjectFactory<Media> {
    private ObjectFactory<Expression> _expressionFactory;
    private ObjectFactory<RuleSet> _ruleSetFactory;

    public ObjectFactory<Expression> getExpressionFactory() {
        return _expressionFactory;
    }

    public void setExpressionFactory(ObjectFactory<Expression> expressionFactory) {
        _expressionFactory = expressionFactory;
    }

    public ObjectFactory<RuleSet> getRuleSetFactory() {
        return _ruleSetFactory;
    }

    public void setRuleSetFactory(ObjectFactory<RuleSet> ruleSetFactory) {
        _ruleSetFactory = ruleSetFactory;
    }

    public Media create(Tree mediaNode, ErrorHandler errorHandler) {
        Media media = new Media();
        media.setLine(mediaNode.getLine());
        media.setChar(mediaNode.getCharPositionInLine());
        for (int idx = 0, numChildren = mediaNode.getChildCount(); idx < numChildren; idx++) {
            Tree child = mediaNode.getChild(idx);
            switch (child.getType()) {
                case MEDIA_EXPR:
                    media.addMedium(concatChildNodeText(child));
                    break;

                case RULESET:
                    RuleSet ruleSet = getRuleSetFactory().create(child, errorHandler);
                    if (ruleSet != null) {
                        media.addBodyElement(ruleSet);
                    }
                    break;

                case VAR:
                    Expression expr = getExpressionFactory().create(child.getChild(1), errorHandler);
                    if (expr != null) {
                        media.setVariable(child.getChild(0).getText(), expr);
                    }
                    break;

                default:
                    handleUnexpectedChild("Unexpected media child:", child);
                    break;
            }
        }

        // ignore empty media elements
        return media.getBodyElements() != null && media.getBodyElements().size() > 0 ? media : null;
    }
}
