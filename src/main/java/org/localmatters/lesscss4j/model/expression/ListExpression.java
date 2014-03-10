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
package org.localmatters.lesscss4j.model.expression;

import java.util.ArrayList;
import java.util.List;
import org.localmatters.lesscss4j.model.AbstractElement;
import org.localmatters.lesscss4j.transform.EvaluationContext;

public class ListExpression
  extends AbstractElement
  implements Expression
{
  private List<Expression> _expressions;

  public ListExpression()
  {
  }

  public ListExpression( final ListExpression copy )
  {
    super( copy );
    if ( null != copy._expressions )
    {
      _expressions = new ArrayList<>( copy._expressions.size() );
      for ( final Expression argument : copy._expressions )
      {
        _expressions.add( argument.clone() );
      }
    }
  }

  public List<Expression> getExpressions()
  {
    return _expressions;
  }

  public void setExpressions( final List<Expression> expressions )
  {
    _expressions = expressions;
  }

  public void addExpression( final Expression expression )
  {
    if ( null == _expressions )
    {
      _expressions = new ArrayList<>();
    }
    _expressions.add( expression );
  }

  public Expression evaluate( final EvaluationContext context )
  {
    final StringBuilder buf = new StringBuilder();
    for ( final Expression expression : _expressions )
    {
      buf.append( expression.evaluate( context ).toString() );
    }
    return new LiteralExpression( buf.toString() );
  }

  public ListExpression clone()
  {
    return new ListExpression( this );
  }
}
