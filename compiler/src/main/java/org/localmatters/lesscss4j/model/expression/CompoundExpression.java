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

import org.localmatters.lesscss4j.error.LessCssException;
import org.localmatters.lesscss4j.model.AbstractElement;
import org.localmatters.lesscss4j.transform.EvaluationContext;

public abstract class CompoundExpression
  extends AbstractElement
  implements Expression
{
  private Expression _left;
  private Expression _right;

  public CompoundExpression()
  {
    this( null, null );
  }

  public CompoundExpression( final Expression left, final Expression right )
  {
    setLeft( left );
    setRight( right );
  }

  public CompoundExpression( final CompoundExpression copy )
  {
    super( copy );
    _left = copy._left.clone();
    _right = copy._right.clone();
  }

  public Expression evaluate( final EvaluationContext context )
  {
    final Expression left = getLeft().evaluate( context );
    final Expression right = getRight().evaluate( context );
    if ( left instanceof ConstantExpression && right instanceof ConstantExpression )
    {
      try
      {
        final ConstantValue result =
          evaluate( ( (ConstantExpression) left ).getValue(),
                    ( (ConstantExpression) right ).getValue() );
        if ( null != result )
        {
          return new ConstantExpression( result );
        }
      }
      catch ( final LessCssException ex )
      {
        if ( null == ex.getPosition() )
        {
          ex.setPosition( left );
        }
        throw ex;
      }
    }
    return this;
  }

  public ConstantValue evaluate( final ConstantValue left, final ConstantValue right )
  {
    return null;
  }

  public Expression getLeft()
  {
    return _left;
  }

  public void setLeft( final Expression left )
  {
    _left = left;
  }

  public Expression getRight()
  {
    return _right;
  }

  public void setRight( final Expression right )
  {
    _right = right;
  }

  public CompoundExpression clone()
  {
    throw new UnsupportedOperationException( "Subclasses must override clone()" );
  }
}
