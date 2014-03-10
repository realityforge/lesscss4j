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
import org.localmatters.lesscss4j.model.AbstractElement;
import org.localmatters.lesscss4j.model.expression.AddExpression;
import org.localmatters.lesscss4j.model.expression.ConstantExpression;
import org.localmatters.lesscss4j.model.expression.DivideExpression;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.model.expression.FunctionExpression;
import org.localmatters.lesscss4j.model.expression.ListExpression;
import org.localmatters.lesscss4j.model.expression.LiteralExpression;
import org.localmatters.lesscss4j.model.expression.MultiplyExpression;
import org.localmatters.lesscss4j.model.expression.SubtractExpression;
import org.localmatters.lesscss4j.model.expression.VariableReferenceExpression;
import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class ExpressionFactory
  extends AbstractObjectFactory<Expression>
{
  public Expression create( final Tree expression, final ErrorHandler errorHandler )
  {
    switch ( expression.getType() )
    {
      case FUNCTION:
        return createFunction( expression, errorHandler );

      case EXPR:
        if ( expression.getChildCount() > 1 )
        {
          return createListExpression( expression, errorHandler );
        }
        else
        {
          return createExpression( expression.getChild( 0 ), errorHandler );
        }

      case LITERAL:
        return createLiteral( expression, errorHandler );

      default:
        handleUnexpectedChild( "Unexpected expression type", expression );
        return null; // shouldn't get here
    }
  }

  protected LiteralExpression createLiteral( final Tree expression, final ErrorHandler errorHandler )
  {
    return createLiteral( concatChildNodeText( expression ), expression, errorHandler );
  }

  protected LiteralExpression createLiteral( final String text, final Tree expression, final ErrorHandler errorHandler )
  {
    final LiteralExpression literal = new LiteralExpression( text );
    literal.setType( expression.getType() );
    literal.setLine( expression.getLine() );
    literal.setChar( expression.getCharPositionInLine() );
    return literal;
  }

  protected Expression createListExpression( final Tree expression, final ErrorHandler errorHandler )
  {
    final ListExpression listExpr = new ListExpression();
    for ( int idx = 0, numChildren = expression.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = expression.getChild( idx );
      switch ( child.getType() )
      {
        case COMMA:
        case WS:
          listExpr.addExpression( createLiteral( child.getText(), child, errorHandler ) );
          break;

        default:
          listExpr.addExpression( createExpression( child, errorHandler ) );
          break;
      }
    }

    return listExpr;
  }

  protected Expression createExpression( final Tree expression, final ErrorHandler errorHandler )
  {
    final Expression result;
    switch ( expression.getType() )
    {
      case CONSTANT:
        result = new ConstantExpression( concatChildNodeText( expression ) );
        break;

      case LITERAL:
        result = createLiteral( expression, errorHandler );
        break;

      case STAR:
        result = new MultiplyExpression( createExpression( expression.getChild( 0 ), errorHandler ),
                                         createExpression( expression.getChild( 1 ), errorHandler ) );
        break;

      case SOLIDUS:
        result = new DivideExpression( createExpression( expression.getChild( 0 ), errorHandler ),
                                       createExpression( expression.getChild( 1 ), errorHandler ) );
        break;

      case PLUS:
        result = new AddExpression( createExpression( expression.getChild( 0 ), errorHandler ),
                                    createExpression( expression.getChild( 1 ), errorHandler ) );
        break;

      case MINUS:
        result = new SubtractExpression( createExpression( expression.getChild( 0 ), errorHandler ),
                                         createExpression( expression.getChild( 1 ), errorHandler ) );
        break;

      case VAR:
        result = new VariableReferenceExpression( expression.getChild( 0 ).getText() );
        break;

      case EXPR:
        result = createExpression( expression.getChild( 0 ), errorHandler );
        break;

      default:
        handleUnexpectedChild( "Unexpected expression type", expression );
        return null; // shouldn't get here
    }

    if ( result instanceof AbstractElement )
    {
      ( (AbstractElement) result ).setLine( expression.getLine() );
      ( (AbstractElement) result ).setChar( expression.getCharPositionInLine() );
    }

    return result;
  }

  protected Expression createFunction( final Tree function, final ErrorHandler errorHandler )
  {
    final Tree nameNode = function.getChild( 0 );
    final FunctionExpression func = new FunctionExpression( concatChildNodeText( nameNode ) );
    for ( int idx = 1, numChildren = function.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = function.getChild( idx );
      switch ( child.getType() )
      {
        case OPEQ:
        case COLON:
          if ( child.getChildCount() == 0 )
          {
            func.addArgument( createLiteral( child.getText(), child, errorHandler ) );
          }
          else
          {
            final Tree propNode = child.getChild( 0 );
            final String prop = propNode.getText();
            final Expression expr = create( child.getChild( 1 ), null );
            func.addArgument( createLiteral( prop, propNode, errorHandler ) );
            func.addArgument( createLiteral( child.getText(), child, errorHandler ) );
            func.addArgument( expr );
          }
          break;

        case FUNCTION:
          func.addArgument( createFunction( child, errorHandler ) );
          break;

        case VAR:
        case LITERAL:
        case EXPR:
          func.addArgument( createExpression( child, errorHandler ) );
          break;

        default:
          func.addArgument( createLiteral( child.getText(), child, errorHandler ) );
          break;

      }
    }

    func.setLine( function.getLine() );
    func.setChar( function.getCharPositionInLine() );
    return func;
  }
}
