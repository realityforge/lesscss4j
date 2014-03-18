package org.localmatters.lesscss4j.transform.function;

import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.transform.function2.ColorFunctions;

public class Argb
  extends AbstractFunction
{
  @Override
  public Expression evaluate( final String name, final Expression... args )
  {
    return new ColorFunctions().argb( getColor( name, 0, args ) );
  }
}
