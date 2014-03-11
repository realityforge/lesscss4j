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
package org.localmatters.lesscss4j.transform;

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.localmatters.lesscss4j.error.ErrorUtils;
import org.localmatters.lesscss4j.error.LessCssException;
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.PositionAware;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class DeclarationTransformer
  extends AbstractTransformer<Declaration>
{
  public List<Declaration> transform( @Nonnull final Declaration declaration,
                                      @Nonnull final EvaluationContext context,
                                      @Nonnull final TransformerManager transformerManager )
  {
    if ( null == declaration.getValues() )
    {
      return null;
    }

    final Declaration transformed = new Declaration( declaration, false );
    for ( final Object value : declaration.getValues() )
    {
      transformed.addValue( transformDeclarationValue( value, context, transformerManager ) );
    }

    return Arrays.asList( transformed );
  }

  protected Object transformDeclarationValue( final Object value,
                                              final EvaluationContext context,
                                              final TransformerManager transformerManager )
  {
    if ( value instanceof Expression )
    {
      try
      {
        Expression expression = (Expression) value;
        final Transformer<Expression> expressionTransformer = transformerManager.getTransformer( expression );
        if ( null != expressionTransformer )
        {
          // Can't think of a reason why we'd ever want to return more than one expression.
          expression = expressionTransformer.transform( expression, context, transformerManager ).get( 0 );
        }
        return expression.evaluate( context );
      }
      catch ( final LessCssException lce )
      {
        ErrorUtils.handleError( context.getErrorHandler(), (PositionAware) value, lce );
      }
    }
    return value;
  }
}
