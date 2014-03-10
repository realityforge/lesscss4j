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
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.model.expression.LiteralExpression;

/**
 * Function that outputs the given string literally, without any further evaluation.
 * <p/>
 * Usage: e("some weird IE expression")
 */
public class Escape
  implements Function
{
  public Expression evaluate( final String name, final Expression... args )
  {
    final int numArgs = args.length;
    if ( numArgs != 1 )
    {
      throw new FunctionException( "Unexpected number of arguments to function %s: %d", name, numArgs );
    }

    String value = args[ 0 ].toString();
    if ( value.length() >= 2 && value.charAt( 0 ) == '"' && value.charAt( value.length() - 1 ) == '"' )
    {
      value = value.substring( 1, value.length() - 1 );
    }
    return new LiteralExpression( value );
  }
}
