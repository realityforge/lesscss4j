package org.localmatters.lesscss4j.transform.function;

import org.localmatters.lesscss4j.error.FunctionException;
import org.realityforge.css.lizard.model.ColorKeyword;
import org.localmatters.lesscss4j.model.expression.ConstantColor;
import org.localmatters.lesscss4j.model.expression.ConstantExpression;
import org.localmatters.lesscss4j.model.expression.ConstantNumber;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.model.expression.LiteralExpression;

public abstract class AbstractFunction
  implements Function
{
  protected final ConstantColor getColor( final String name, final int index, final Expression... args )
  {
    final Expression expr = args[ index ];
    if ( expr instanceof ConstantExpression && ( (ConstantExpression) expr ).getValue() instanceof ConstantColor )
    {
      return (ConstantColor) ( (ConstantExpression) expr ).getValue();
    }
    else if ( expr instanceof LiteralExpression &&
              null != ColorKeyword.valueOf( ( (LiteralExpression) expr ).getValue() ) )
    {
      final ColorKeyword colorKeyword = ColorKeyword.valueOf( ( (LiteralExpression) expr ).getValue() );
      final ConstantColor constantColor = new ConstantColor();
      constantColor.setValue( colorKeyword.getHexValue() );
      return constantColor;
    }
    else
    {
      throw new FunctionException( "Argument %d for function '%s' must be a color: %s", index + 1, name, expr );
    }
  }

  protected final boolean isColor( final int index, final Expression... args )
  {
    return isColor( args[ index ] );
  }

  protected final boolean isColor( final Expression expr )
  {
    return
      ( expr instanceof ConstantExpression && ( (ConstantExpression) expr ).getValue() instanceof ConstantColor ) ||
      ( expr instanceof LiteralExpression && null != ColorKeyword.valueOf( ( (LiteralExpression) expr ).getValue() ) );
  }

  protected final ConstantNumber getNumber( final String name, final int index, final Expression... args )
  {
    final Expression expr = args[ index ];
    if ( !isNumber( expr ) )
    {
      throw new FunctionException( "Argument %d for function '%s' must be a number: %s", index + 1, name, expr );
    }
    return (ConstantNumber) ( (ConstantExpression) expr ).getValue();
  }

  protected final boolean isNumber( final Expression arg )
  {
    return arg instanceof ConstantExpression &&
           ( (ConstantExpression) arg ).getValue() instanceof ConstantNumber;
  }
}
