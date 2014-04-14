package org.localmatters.lesscss4j.transform.function2;

import java.math.BigDecimal;
import org.localmatters.lesscss4j.model.expression.ConstantColor;
import org.localmatters.lesscss4j.model.expression.ConstantExpression;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.realityforge.css.lizard.CssFunction;
import org.realityforge.css.lizard.model.NumberValue;
import org.realityforge.css.lizard.model.Percentage;

/**
 * Class defining Color-oriented css functions.
 */
public class ColorFunctions
{
 private static final BigDecimal V_360 = BigDecimal.valueOf( 360 );
 private static final BigDecimal V_100 = BigDecimal.valueOf( 100 );

  @CssFunction( description = "Function to change the hue of a color by a given number of degrees on the color wheel.",
                usage = "spin(@color, 10)" )
  public Expression spin( final ConstantColor color, final NumberValue value )
  {
    final float[] hsla = color.toHSL();
    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ] + (float) value.getValue().remainder( V_360 ).doubleValue(),
                     hsla[ 1 ],
                     hsla[ 2 ] );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to increase the saturation of a color.",
                usage = "saturate(@color, 10%)" )
  public Expression saturate( final ConstantColor color, final Percentage value )
  {
    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ],
                     hsla[ 1 ] + ( (float) value.getNumber().getValue().divide( V_100 ).doubleValue() ),
                     hsla[ 2 ] );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to lighten a color.",
                usage = "lighten(@color, 50%)" )
  public Expression lighten( final ConstantColor color, final Percentage value )
  {
    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ],
                     hsla[ 1 ],
                     hsla[ 2 ] + ( (float) value.getNumber().getValue().divide( V_100 ).doubleValue() ) );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function that reduces the saturation of a color by a percentage.",
                usage = "desaturate(@color, 10%)" )
  public Expression desaturate( final ConstantColor color, final Percentage value )
  {
    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ],
                     hsla[ 1 ] - (float) value.getNumber().getValue().divide( V_100 ).doubleValue(),
                     hsla[ 2 ] );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to convert a color to grayscale (i.e. desaturate 100%).",
                usage = "grayscale(@color)" )
  public Expression grayscale( final ConstantColor color )
  {
    return greyscale( color );
  }

  @CssFunction( description = "Function to convert a color to grayscale (i.e. desaturate 100%).",
                usage = "grayscale(@color)" )
  public Expression greyscale( final ConstantColor color )
  {
    return desaturate( color, new Percentage( new NumberValue( V_100 ) ) );
  }

  @CssFunction( description = "Function to make a color darker by a percentage (i.e. reduce the value).",
                usage = "darken(@color, 10%)" )
  public Expression darken( final ConstantColor color, final Percentage value )
  {
    final float[] hsla = color.toHSL();

    final ConstantColor newColor = new ConstantColor();
    newColor.setHSL( hsla[ 0 ],
                     hsla[ 1 ],
                     hsla[ 2 ] - ( (float) value.getNumber().getValue().divide( V_100 ).doubleValue() ) );
    return new ConstantExpression( newColor );
  }

  @CssFunction( description = "Function to convert color to argb format." )
  public Expression argb( final ConstantColor color )
  {
    return new ConstantExpression( color.toARGB() );
  }
}
