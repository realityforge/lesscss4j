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
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.MixinReference;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.Selector;
import org.localmatters.lesscss4j.model.expression.Expression;
import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class RuleSetFactory
  extends AbstractObjectFactory<RuleSet>
{
  private ObjectFactory<Declaration> _declarationFactory;
  private ObjectFactory<Selector> _selectorFactory;
  private ObjectFactory<Expression> _expressionFactory;

  public ObjectFactory<Expression> getExpressionFactory()
  {
    return _expressionFactory;
  }

  public void setExpressionFactory( final ObjectFactory<Expression> expressionFactory )
  {
    _expressionFactory = expressionFactory;
  }

  public ObjectFactory<Declaration> getDeclarationFactory()
  {
    return _declarationFactory;
  }

  public void setDeclarationFactory( final ObjectFactory<Declaration> declarationFactory )
  {
    _declarationFactory = declarationFactory;
  }

  public ObjectFactory<Selector> getSelectorFactory()
  {
    return _selectorFactory;
  }

  public void setSelectorFactory( final ObjectFactory<Selector> selectorFactory )
  {
    _selectorFactory = selectorFactory;
  }

  public RuleSet create( final Tree ruleSetNode, final ErrorHandler errorHandler )
  {
    final RuleSet ruleSet = new RuleSet();
    ruleSet.setLine( ruleSetNode.getLine() );
    ruleSet.setChar( ruleSetNode.getCharPositionInLine() );

    for ( int idx = 0, numChildren = ruleSetNode.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = ruleSetNode.getChild( idx );
      switch ( child.getType() )
      {
        case SELECTOR:
          final Selector selector = getSelectorFactory().create( child, errorHandler );
          if ( null != selector )
          {
            ruleSet.addSelector( selector );
          }
          break;

        case MIXIN_ARG:
        case VAR:
          final Expression expr = getExpressionFactory().create( child.getChild( 1 ), null );
          if ( null != expr )
          {
            final String varName = child.getChild( 0 ).getText();
            if ( null != ruleSet.getVariable( varName ) )
            {
              // todo: error -- duplicate error
            }
            ruleSet.setVariable( varName, expr );

            if ( child.getType() == MIXIN_ARG )
            {
              ruleSet.addArgument( varName, expr );
            }
          }
          break;

        case DECLARATION:
          final Declaration declaration = getDeclarationFactory().create( child, errorHandler );
          if ( null != declaration )
          {
            ruleSet.addDeclaration( declaration );
          }
          break;

        case MIXIN_REF:
          final MixinReference ref = createMixinReferences( child, errorHandler );
          if ( null != ref )
          {
            ruleSet.addDeclaration( ref );
          }
          break;

        case RULESET:
          final RuleSet childRuleSet = create( child, errorHandler );
          if ( null != childRuleSet )
          {
            ruleSet.addRuleSet( childRuleSet, -1 );
          }
          break;

        default:
          handleUnexpectedChild( "Unexpected ruleset child:", child );
          break;
      }
    }

    return ruleSet;
  }

  protected MixinReference createMixinReferences( final Tree mixinNode, final ErrorHandler errorHandler )
  {
    // todo: put this in it's own factory?

    final MixinReference ref = new MixinReference();
    ref.setLine( mixinNode.getLine() );
    ref.setChar( mixinNode.getCharPositionInLine() );

    for ( int idx = 0, numChildren = mixinNode.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = mixinNode.getChild( idx );
      switch ( child.getType() )
      {
        case SELECTOR:
          final Selector selector = getSelectorFactory().create( child, errorHandler );
          ref.setSelector( selector );
          break;

        case MIXIN_ARG:
          final Expression arg = getExpressionFactory().create( child.getChild( 0 ), errorHandler );
          if ( null != arg )
          {
            ref.addArgument( arg );
          }
          break;

        default:
          handleUnexpectedChild( "Unexpected mixin reference child", child );
      }
    }
    return ref;
  }
}
