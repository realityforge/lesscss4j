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
import org.localmatters.lesscss4j.model.expression.ConstantExpression;
import org.localmatters.lesscss4j.model.expression.ConstantNumber;
import org.localmatters.lesscss4j.model.expression.Expression;

/**
 * Function that reduces the saturation of a color by a percentage.
 * <p/>
 * Usage: desaturate(@color, 10%)
 */
public class Desaturate
  extends AbstractColorFunction
{
  @Override
  protected Expression evaluate( final ConstantColor color, final ConstantNumber value )
  {
    if ( null != value.getUnit() && !"%".equals( value.getUnit() ) )
    {
      throw new FunctionException( "Argument 2 for function '%s' must be a percentage: %s", value.toString() );
    }

    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ], hsla[ 1 ] - ( (float) value.getValue() / 100.0f ), hsla[ 2 ] );
    return new ConstantExpression( newColor );
  }
}
