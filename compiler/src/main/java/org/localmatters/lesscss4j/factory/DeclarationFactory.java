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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.Selector;
import org.localmatters.lesscss4j.model.expression.AccessorExpression;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.model.expression.FunctionExpression;
import org.localmatters.lesscss4j.model.expression.LiteralExpression;
import org.localmatters.lesscss4j.parser.antlr.LessCssLexer;
import org.localmatters.lesscss4j.parser.antlr.LessCssParser;
import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class DeclarationFactory
  extends AbstractObjectFactory<Declaration>
{
  private ObjectFactory<Expression> _expressionFactory;
  private ObjectFactory<Selector> _selectorFactory;

  public ObjectFactory<Selector> getSelectorFactory()
  {
    return _selectorFactory;
  }

  public void setSelectorFactory( final ObjectFactory<Selector> selectorFactory )
  {
    _selectorFactory = selectorFactory;
  }

  public ObjectFactory<Expression> getExpressionFactory()
  {
    return _expressionFactory;
  }

  public void setExpressionFactory( final ObjectFactory<Expression> expressionFactory )
  {
    _expressionFactory = expressionFactory;
  }

  @Nullable
  public Declaration create( @Nonnull final Tree declarationNode, @Nullable final ErrorHandler errorHandler )
  {
    final Declaration declaration = new Declaration();
    declaration.setLine( declarationNode.getLine() );
    declaration.setChar( declarationNode.getCharPositionInLine() );

    for ( int idx = 0, numChildren = declarationNode.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = declarationNode.getChild( idx );
      switch ( child.getType() )
      {
        case IDENT:
        case FONT:
          String propName = child.getText();
          final Tree propChild = child.getChild( 0 );
          if ( null != propChild )
          {
            propName = propChild.getText() + propName;
          }
          declaration.setProperty( propName );
          break;

        case PROP_VALUE:
          final List<Object> values = createPropValues( child, errorHandler );
          if ( null != values )
          {
            declaration.setValues( values );
          }
          break;

        case IMPORTANT_SYM:
          declaration.setImportant( true );
          break;

        default:
          handleUnexpectedChild( "Unexpected declaration child:", child );
          break;
      }
    }

    return null == declaration.getValues() ? null : declaration;
  }

  protected List<Object> createPropValues( final Tree valueNode, final ErrorHandler errorHandler )
  {
    final int numChildren = valueNode.getChildCount();
    final List<Object> values = new ArrayList<>( numChildren );
    for ( int idx = 0; idx < numChildren; idx++ )
    {
      final Tree child = valueNode.getChild( idx );
      switch ( child.getType() )
      {
        case LITERAL:
        case EXPR:
        case EXPRESSION:
        case FUNCTION:
        {
          Expression expression = getExpressionFactory().create( child, errorHandler );
          if ( null != expression )
          {
            final Expression ieFilter = parseIE8AlphaFilter( expression, errorHandler );
            if ( null != ieFilter )
            {
              expression = ieFilter;
            }
            values.add( expression );
          }
          break;
        }

        case MIXIN_ACCESSOR:
        {
          final AccessorExpression accessor = createMixinAccessor( child, errorHandler );
          if ( null != accessor )
          {
            values.add( accessor );
          }
          break;
        }

        case COMMA:
        case NUMBER:
        case SOLIDUS:
        case IDENT:
        case STRING:
          // These tokens just spit out as is
          values.add( child.getText() );
          break;

        case WS:
          values.add( " " );
          break;

        default:
          handleUnexpectedChild( "Unexpected declaration value child:", child );
          break;
      }
    }
    return values.size() > 0 ? values : null;
  }

  protected AccessorExpression createMixinAccessor( final Tree accessorNode, final ErrorHandler errorHandler )
  {
    final AccessorExpression expression = new AccessorExpression();
    expression.setSelector( getSelectorFactory().create( accessorNode.getChild( 0 ), errorHandler ) );
    final Tree propertyNode = accessorNode.getChild( 1 );
    switch ( propertyNode.getType() )
    {
      case VAR:
        expression.setProperty( propertyNode.getChild( 0 ).getText() );
        expression.setVariable( true );
        break;

      case STRING:
        final String str = propertyNode.getText();
        expression.setProperty( str.substring( 1, str.length() - 1 ) ); // strip off quotes
        expression.setVariable( false );
        break;

      default:
        expression.setProperty( propertyNode.getText() );
        expression.setVariable( false );
        break;
    }

    return expression;
  }

  private final Pattern _ieAlphaPattern =
    Pattern.compile( "(?i)['\"]progid:DXImageTransform\\.Microsoft\\.(Alpha\\(.*\\))['\"]" );

  /**
   * We can't handle the IE8 way of processing Alpha in the Lexer...it just looks like a literal string.  This method
   * takes the LiteralExpression and attempts to parse it as a declaration property value so that variables and
   * expressions can be used in the opacity value.
   *
   * @param value The Literal expression to parse
   * @return The parsed Expression.  Null if it isn't an Alpha expression or it cannot be parsed.
   */
  protected Expression parseIE8AlphaFilter( final Expression value, final ErrorHandler errorHandler )
  {
    if ( value instanceof LiteralExpression )
    {
      final String text = ( (LiteralExpression) value ).getValue();

      // Short circuit test to avoid doing the regex match if we don't have to
      if ( ( text.charAt( 0 ) == '"' || text.charAt( 0 ) == '\'' ) &&
           text.length() > "'progid:DXImageTransform.Microsoft.Alpha()'".length() &&
           ( text.charAt( text.length() - 1 ) == '"' || text.charAt( text.length() - 1 ) == '\'' ) )
      {
        final Matcher matcher = _ieAlphaPattern.matcher( text );
        if ( matcher.matches() )
        {
          final LessCssLexer lexer = new LessCssLexer( new ANTLRStringStream( matcher.group( 1 ) ) );
          final LessCssParser parser = new LessCssParser( new CommonTokenStream( lexer ) );
          try
          {
            final LessCssParser.propertyValue_return result = parser.propertyValue();
            final List<Object> propValues = createPropValues( (Tree) result.getTree(), errorHandler );
            final FunctionExpression alphaFunction = (FunctionExpression) propValues.get( 0 );
            alphaFunction.setName( "progid:DXImageTransform.Microsoft.Alpha" );
            alphaFunction.setQuoted( true );
            return alphaFunction;
          }
          catch ( final RecognitionException e )
          {
            // todo: send something to the error handler
            // Can't do anything with it.  Just leave it alone
          }
        }
      }
    }
    return null;
  }
}
