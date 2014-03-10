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

import org.localmatters.lesscss4j.model.expression.ConstantColor;
import org.localmatters.lesscss4j.model.expression.ConstantNumber;
import org.localmatters.lesscss4j.model.expression.Expression;

/**
 * Function to convert a color to grayscale (i.e. desaturate 100%)
 * <p/>
 * Usage: grayscale(@color)
 */
public class Grayscale
  extends Desaturate
{
  public Grayscale()
  {
    setValueRequired( false );
  }

  @Override
  protected Expression evaluate( final ConstantColor color, final ConstantNumber value )
  {
    return super.evaluate( color, new ConstantNumber( 100, "%" ) );
  }
}
