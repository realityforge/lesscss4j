package org.localmatters.lesscss4j.transform.function2;

import org.localmatters.lesscss4j.error.FunctionException;
import org.localmatters.lesscss4j.model.expression.ConstantColor;
import org.localmatters.lesscss4j.model.expression.ConstantExpression;
import org.localmatters.lesscss4j.model.expression.ConstantNumber;
import org.localmatters.lesscss4j.model.expression.Expression;

/**
 * Class defining Color-oriented css functions.
 */
public class ColorFunctions
{
  @CssFunction( description = "Function to change the hue of a color by a given number of degrees on the color wheel.",
                usage = "spin(@color, 10)" )
  public Expression spin( final ConstantColor color, final ConstantNumber value )
  {
    final float[] hsla = color.toHSL();
    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( ( ( hsla[ 0 ] + (float) value.getValue() ) % 360.0f ), hsla[ 1 ], hsla[ 2 ] );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to increase the saturation of a color.",
                usage = "saturate(@color, 10%)" )
  public Expression saturate( final ConstantColor color, final ConstantNumber value )
  {
    if ( null != value.getUnit() && !"%".equals( value.getUnit() ) )
    {
      throw new FunctionException( "Argument 2 for function '%s' must be a percentage: %s", value.toString() );
    }

    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ], hsla[ 1 ] + ( (float) value.getValue() / 100.0f ), hsla[ 2 ] );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to lighten a color.",
                usage = "lighten(@color, 50%)" )
  public Expression lighten( final ConstantColor color, final ConstantNumber value )
  {
    if ( null != value.getUnit() && !"%".equals( value.getUnit() ) )
    {
      throw new FunctionException( "Argument 2 for function '%s' must be a percentage: %s", value.toString() );
    }

    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ], hsla[ 1 ], hsla[ 2 ] + ( (float) value.getValue() / 100.0f ) );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function that reduces the saturation of a color by a percentage.",
                usage = "desaturate(@color, 10%)" )
  public Expression desaturate( final ConstantColor color, final ConstantNumber value )
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

  @CssFunction( description = "Function to convert a color to grayscale (i.e. desaturate 100%).",
                usage = "grayscale(@color)" )
  public Expression grayscale( final ConstantColor color )
  {
    return desaturate( color, new ConstantNumber( 100, "%" ) );
  }

  @CssFunction( description = "Function to make a color darker by a percentage (i.e. reduce the value).",
                usage = "darken(@color, 10%)" )
  public Expression darken( final ConstantColor color, final ConstantNumber value )
  {
    if ( null != value.getUnit() && !"%".equals( value.getUnit() ) )
    {
      throw new FunctionException( "Argument 2 for function '%s' must be a percentage: %s", value.toString() );
    }

    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ], hsla[ 1 ], hsla[ 2 ] - ( (float) value.getValue() / 100.0f ) );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to convert color to argb format." )
  public Expression argb( final ConstantColor color )
  {
    return new ConstantExpression( color.toARGB() );
  }
}
