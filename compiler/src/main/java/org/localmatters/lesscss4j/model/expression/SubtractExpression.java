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

public class SubtractExpression
  extends CompoundExpression
{
  public SubtractExpression( final SubtractExpression copy )
  {
    super( copy );
  }

  public SubtractExpression( final Expression left, final Expression right )
  {
    super( left, right );
  }

  @Override
  public ConstantValue evaluate( final ConstantValue left, final ConstantValue right )
  {
    return left.subtract( right );
  }

  @Override
  public SubtractExpression clone()
  {
    return new SubtractExpression( this );
  }
}
