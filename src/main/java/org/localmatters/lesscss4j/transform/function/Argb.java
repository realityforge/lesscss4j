package org.localmatters.lesscss4j.transform.function;

import org.localmatters.lesscss4j.error.FunctionException;
import org.localmatters.lesscss4j.model.expression.ConstantExpression;
import org.localmatters.lesscss4j.model.expression.Expression;

/**
 * Function to convert color to argb format.
 */
public class Argb
  extends AbstractFunction
{
  @Override
  public Expression evaluate( final String name, final Expression... args )
  {
    if ( 1 != args.length || isColor( 0, args ) )
    {
      throw new FunctionException( "Function '%s' expects a single argument of type color.", name );
    }
    return new ConstantExpression( getColor( name, 0, args ).toARGB() );
  }
}
