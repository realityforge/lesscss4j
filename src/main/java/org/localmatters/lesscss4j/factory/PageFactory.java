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
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.Page;
import org.localmatters.lesscss4j.model.expression.Expression;
import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class PageFactory
  extends AbstractObjectFactory<Page>
{
  private ObjectFactory<Declaration> _declarationFactory;
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

  @Nullable
  public Page create( @Nonnull final Tree pageNode, @Nullable final ErrorHandler errorHandler )
  {
    final Page page = new Page();
    page.setLine( pageNode.getLine() );
    page.setChar( pageNode.getCharPositionInLine() );
    for ( int idx = 0, numChildren = pageNode.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = pageNode.getChild( idx );
      switch ( child.getType() )
      {
        case IDENT:
          page.setPseudoPage( child.getText() );

        case DECLARATION:
          final Declaration declaration = getDeclarationFactory().create( child, errorHandler );
          if ( null != declaration )
          {
            page.addDeclaration( declaration );
          }
          break;

        case VAR:
          final Expression expr = getExpressionFactory().create( child.getChild( 1 ), errorHandler );
          if ( null != expr )
          {
            page.setVariable( child.getChild( 0 ).getText(), expr );
          }
          break;

        default:
          handleUnexpectedChild( "Unexpected page child:", child );
          break;
      }
    }

    return null != page.getDeclarations() && page.getDeclarations().size() > 0 ? page : null;
  }
}
