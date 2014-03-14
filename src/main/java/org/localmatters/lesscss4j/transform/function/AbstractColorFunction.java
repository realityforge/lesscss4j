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
package org.localmatters.lesscss4j.transform.function;

import org.localmatters.lesscss4j.error.FunctionException;
import org.localmatters.lesscss4j.model.expression.ConstantColor;
import org.localmatters.lesscss4j.model.expression.ConstantNumber;
import org.localmatters.lesscss4j.model.expression.Expression;

public abstract class AbstractColorFunction
  extends AbstractFunction
{
  private boolean _valueRequired = true;

  public boolean isValueRequired()
  {
    return _valueRequired;
  }

  public void setValueRequired( final boolean valueRequired )
  {
    _valueRequired = valueRequired;
  }

  public Expression evaluate( final String name, final Expression... args )
  {
    final int numArgs = args.length;
    if ( isValueRequired() && 2 != numArgs || !isValueRequired() && 1 != numArgs )
    {
      throw new FunctionException( "Invalid number of arguments for function '%s'", name );
    }

    final ConstantColor color = getColor( name, 0, args );
    ConstantNumber value = null;
    if ( args.length > 1 )
    {
      value = getNumber( name, 1, args );
    }

    return evaluate( color, value );
  }

  protected abstract Expression evaluate( ConstantColor color, ConstantNumber value );
}
