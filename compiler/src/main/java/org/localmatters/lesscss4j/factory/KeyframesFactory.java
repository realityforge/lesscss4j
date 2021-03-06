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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.tree.Tree;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.model.Keyframes;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.expression.Expression;
import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class KeyframesFactory
  extends AbstractObjectFactory<Keyframes>
{
  private ObjectFactory<Expression> _expressionFactory;
  private ObjectFactory<RuleSet> _ruleSetFactory;

  public ObjectFactory<Expression> getExpressionFactory()
  {
    return _expressionFactory;
  }

  public void setExpressionFactory( final ObjectFactory<Expression> expressionFactory )
  {
    _expressionFactory = expressionFactory;
  }

  public ObjectFactory<RuleSet> getRuleSetFactory()
  {
    return _ruleSetFactory;
  }

  public void setRuleSetFactory( final ObjectFactory<RuleSet> ruleSetFactory )
  {
    _ruleSetFactory = ruleSetFactory;
  }

  @Nullable
  public Keyframes create( @Nonnull final Tree mediaNode, @Nullable final ErrorHandler errorHandler )
  {
    final Keyframes keyframes = new Keyframes();
    keyframes.setLine( mediaNode.getLine() );
    keyframes.setChar( mediaNode.getCharPositionInLine() );
    for ( int idx = 0, numChildren = mediaNode.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = mediaNode.getChild( idx );
      switch ( child.getType() )
      {
        case SELECTOR:
          keyframes.setName( concatChildNodeText( child ) );
          break;

        case RULESET:
          final RuleSet ruleSet = getRuleSetFactory().create( child, errorHandler );
          if ( null != ruleSet )
          {
            keyframes.addBodyElement( ruleSet );
          }
          break;

        case VAR:
          final Expression expr = getExpressionFactory().create( child.getChild( 1 ), errorHandler );
          if ( null != expr )
          {
            keyframes.setVariable( child.getChild( 0 ).getText(), expr );
          }
          break;

        default:
          handleUnexpectedChild( "Unexpected media child:", child );
          break;
      }
    }

    // ignore empty keyframes elements
    return null != keyframes.getBodyElements() && keyframes.getBodyElements().size() > 0 ? keyframes : null;
  }
}
