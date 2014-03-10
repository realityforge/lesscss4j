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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.model.expression.FunctionExpression;
import org.localmatters.lesscss4j.model.expression.LiteralExpression;
import org.localmatters.lesscss4j.transform.function.Function;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class FunctionTransformer
  extends AbstractTransformer<Expression>
{
  private Map<String, Function> _functionMap;

  public FunctionTransformer( @Nonnull final TransformerManager transformerManager )
  {
    super( transformerManager );
  }

  public void addFunction( final String name, final Function function )
  {
    if ( null == _functionMap )
    {
      _functionMap = new LinkedHashMap<>();
    }
    _functionMap.put( name, function );
  }

  public Map<String, Function> getFunctionMap()
  {
    return _functionMap;
  }

  public void setFunctionMap( final Map<String, Function> functionMap )
  {
    _functionMap = functionMap;
  }

  public List<Expression> transform( final Expression expression, final EvaluationContext context )
  {
    if ( !( expression instanceof FunctionExpression ) )
    {
      throw new IllegalArgumentException( "Object to transform must be a FunctionExpression" );
    }

    final FunctionExpression function = (FunctionExpression) expression;
    Expression result = function;

    final String functionName = function.getName();
    final Function func = getFunctionMap().get( functionName );
    if ( null != func )
    {
      final List<Expression> args = new ArrayList<>( function.getArguments().size() );

      // Evaluate each of the argument expressions before calling the function.
      for ( int idx = 0; idx < function.getArguments().size(); idx++ )
      {
        Expression argExpression = function.getArguments().get( idx );
        if ( !( argExpression instanceof LiteralExpression ) || !argExpression.toString().equals( "," ) )
        {
          final Transformer<Expression> transformer = getTransformer( argExpression, false );
          if ( null != transformer )
          {
            argExpression = transformer.transform( argExpression, context ).get( 0 );
          }
          argExpression = argExpression.evaluate( context );
          args.add( argExpression );
        }
      }

      result = func.evaluate( functionName, args.toArray( new Expression[ args.size() ] ) );
    }

    return Arrays.asList( result );
  }
}
