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

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.localmatters.lesscss4j.error.DivideByZeroException;
import org.localmatters.lesscss4j.error.UnitMismatchException;

public class ConstantColor
  implements ConstantValue
{
  private static final BigDecimal ZERO = new BigDecimal( 0 );
  private static final BigDecimal MIN_PERCENTAGE = ZERO;
  private static final BigDecimal MAX_PERCENTAGE = new BigDecimal( 100 );
  private static final BigDecimal MAX_COLOR_COMPONENT_VALUE = new BigDecimal( 255 );
  private static final BigDecimal MAX_ALPHA_VALUE = new BigDecimal( 1 );
  private static final BigDecimal MIN_ALPHA_VALUE = ZERO;

  private int _red;
  private int _green;
  private int _blue;
  private BigDecimal _alpha = MAX_ALPHA_VALUE;
  private boolean _compressed;

  private static final String COLOR_COMPONENT_CONSTANT = "(-?\\d+(\\.\\d+)?||\\d+%?)";
  /**
   * Regular expression that is used to extract the rgb or hsl and alpha values from the color function value
   */
  private static final Pattern RGB_HSL_PATTERN = Pattern.compile(
    "(?i)(?:rgb|hsl)a?\\s*\\(\\s*" +
    COLOR_COMPONENT_CONSTANT +
    "\\s*,\\s*" +
    COLOR_COMPONENT_CONSTANT +
    "\\s*,\\s*" +
    COLOR_COMPONENT_CONSTANT +
    "(?:\\s*,\\s*" +
    COLOR_COMPONENT_CONSTANT +
    ")?\\s*\\)" );

  public ConstantColor()
  {
    this( 0 );
  }

  public ConstantColor( final ConstantColor copy )
  {
    _red = copy._red;
    _green = copy._green;
    _blue = copy._blue;
    _alpha = copy._alpha;
    _compressed = copy._compressed;
  }

  public ConstantColor( final int value )
  {
    setValue( value );
  }

  public ConstantColor( final int red, final int green, final int blue )
  {
    setRed( red );
    setGreen( green );
    setBlue( blue );
  }

  public ConstantColor( String value )
  {
    if ( value.charAt( 0 ) == '#' )
    {
      value = value.substring( 1 ); // strip off the '#'
      if ( value.length() == 3 )
      {
        value = new String( new char[]{ value.charAt( 0 ),
                                        value.charAt( 0 ),
                                        value.charAt( 1 ),
                                        value.charAt( 1 ),
                                        value.charAt( 2 ),
                                        value.charAt( 2 )
        } );
        _compressed = true;
      }
      setValue( Integer.parseInt( value, 16 ) );
    }
    else if ( isRGBFunction( value ) )
    {
      final Matcher matcher = RGB_HSL_PATTERN.matcher( value );
      if ( matcher.matches() )
      {
        final String red = matcher.group( 1 );
        final String green = matcher.group( 3 );
        final String blue = matcher.group( 5 );

        setRed( parseRGBValue( red ) );
        setGreen( parseRGBValue( green ) );
        setBlue( parseRGBValue( blue ) );

        if ( value.charAt( 3 ) == 'a' || value.charAt( 3 ) == 'A' )
        {
          final String alpha = matcher.group( 7 );
          setAlpha( parseAlphaValue( alpha ) );
        }
      }
      else
      {
        throw new IllegalArgumentException( "Invalid RGB color specification: " + value );
      }
    }
    else if ( isHSLFunction( value ) )
    {
      final Matcher matcher = RGB_HSL_PATTERN.matcher( value );
      if ( matcher.matches() )
      {
        if ( value.charAt( 3 ) == 'a' || value.charAt( 3 ) == 'A' )
        {
          final String alpha = matcher.group( 7 );
          setAlpha( new BigDecimal( alpha ) );
        }

        final float hue = Integer.parseInt( matcher.group( 1 ) );
        final BigDecimal saturation = parsePercentage( matcher.group( 3 ) );
        final BigDecimal lightness = parsePercentage( matcher.group( 5 ) );

        setHSL( hue, saturation.floatValue(), lightness.floatValue() );
      }
      else
      {
        throw new IllegalArgumentException( "Invalid HSL color specification: " + value );
      }
    }
    else
    {
      final ColorKeyword keywordValue = ColorKeyword.valueOf( value.toLowerCase() );
      if ( null != keywordValue )
      {
        setValue( keywordValue.getHexValue() );
      }
    }
  }


  public void setHSL( float hue, float saturation, float lightness )
  {
    // Normalize hue to the range [0, 360)
    if ( hue < 0 || hue >= 360 )
    {
      hue = ( ( hue % 360 ) + 360 ) % 360;
    }

    // Convert hue into the range [0, 1]
    hue /= 360.0;

    saturation = Math.min( 1, Math.max( saturation, 0.0f ) );
    lightness = Math.min( 1, Math.max( lightness, 0.0f ) );

    final int[] rgb = hslToRgb( hue, saturation, lightness );
    setRed( rgb[ 0 ] );
    setGreen( rgb[ 1 ] );
    setBlue( rgb[ 2 ] );
  }

  /**
   * Converts an HSL color value to RGB. Conversion formula adapted from http://en.wikipedia.org/wiki/HSL_color_space.
   * Assumes h, s, and l are contained in the set [0, 1] and returns r, g, and b in the set [0, 255].
   *
   * @param h The hue
   * @param s The saturation
   * @param l The lightness
   * @return Integer array with the RGB representation.  0=red, 1=green, 2=blue
   */
  protected static int[] hslToRgb( final float h, final float s, final float l )
  {
    final float r;
    final float g;
    final float b;

    if ( s == 0 )
    {
      r = g = b = l; // achromatic
    }
    else
    {
      final float q = l < 0.5f ? l * ( 1f + s ) : l + s - l * s;
      final float p = 2 * l - q;
      r = hueToRgb( p, q, h + 1f / 3f );
      g = hueToRgb( p, q, h );
      b = hueToRgb( p, q, h - 1f / 3f );
    }

    return new int[]{ Math.round( r * 255f ), Math.round( g * 255f ), Math.round( b * 255f ) };
  }

  protected static float hueToRgb( final float p, final float q, float t )
  {
    if ( t < 0f )
    {
      t += 1.0;
    }
    if ( t > 1f )
    {
      t -= 1.0;
    }
    if ( t < 1f / 6f )
    {
      return p + ( q - p ) * 6f * t;
    }
    if ( t < 1f / 2f )
    {
      return q;
    }
    if ( t < 2f / 3f )
    {
      return p + ( q - p ) * ( 2f / 3f - t ) * 6f;
    }
    return p;
  }

  protected BigDecimal parsePercentage( String value )
  {
    if ( value.endsWith( "%" ) )
    {
      // Strip off the optional percent sign
      value = value.substring( 0, value.length() - 1 );
    }
    return new BigDecimal( value ).
      max( MIN_PERCENTAGE ).
      min( MAX_PERCENTAGE ).
      divide( MAX_PERCENTAGE );
  }

  protected int parseRGBValue( final String value )
  {
    if ( value.charAt( value.length() - 1 ) == '%' )
    {
      // colors are in terms of percentage
      return round( parsePercentage( value ).multiply( MAX_COLOR_COMPONENT_VALUE ) );
    }
    else
    {
      return round( new BigDecimal( value ) );
    }
  }

  private int round( final BigDecimal value )
  {
    return value.round( MathContext.DECIMAL32 ).intValue();
  }

  protected BigDecimal parseAlphaValue( final String value )
  {
    if ( value.charAt( value.length() - 1 ) == '%' )
    {
      return parsePercentage( value );
    }
    else
    {
      return new BigDecimal( value );
    }
  }

  public void setValue( final int value )
  {
    setRed( ( value & 0xff0000 ) >> 16 );
    setGreen( ( value & 0x00ff00 ) >> 8 );
    setBlue( value & 0x0000ff );
  }

  public double getValue()
  {
    return ( getRed() << 16 ) | ( getGreen() << 8 ) | getBlue();
  }

  public float[] toHSL()
  {
    final float r = _red / 255.0f;
    final float g = _green / 255.0f;
    final float b = _blue / 255.0f;

    final float max = Math.max( Math.max( r, g ), b );
    final float min = Math.min( Math.min( r, g ), b );

    float h;
    final float s;
    final float l;

    l = ( max + min ) / 2;
    final float d = max - min;

    if ( max == min )
    {
      h = s = 0;
    }
    else
    {
      s = l > 0.5 ? d / ( 2 - max - min ) : d / ( max + min );
      if ( max == r )
      {
        h = ( g - b ) / d + ( g < b ? 6 : 0 );
      }
      else if ( max == g )
      {
        h = ( b - r ) / d + 2;
      }
      else
      { // if (max == b) {
        h = ( r - g ) / d + 4;
      }

      h /= 6;
    }

    return new float[]{ h * 360, s, l, null == _alpha ? 1.0f : _alpha.floatValue() };
  }

  protected void checkUnits( final ConstantValue that )
  {
    if ( that instanceof ConstantNumber && null != ( (ConstantNumber) that ).getUnit() )
    {
      throw new UnitMismatchException( this, that );
    }
  }

  public ConstantValue add( final ConstantValue right )
  {
    checkUnits( right );
    if ( right instanceof ConstantNumber )
    {
      return new ConstantColor( (int) ( getRed() + right.getValue() ),
                                (int) ( getGreen() + right.getValue() ),
                                (int) ( getBlue() + right.getValue() ) );
    }
    else
    {
      final ConstantColor color = (ConstantColor) right;
      return new ConstantColor( getRed() + color.getRed(),
                                getGreen() + color.getGreen(),
                                getBlue() + color.getBlue() );
    }
  }

  public ConstantValue subtract( final ConstantValue right )
  {
    checkUnits( right );
    if ( right instanceof ConstantNumber )
    {
      return new ConstantColor( (int) ( getRed() - right.getValue() ),
                                (int) ( getGreen() - right.getValue() ),
                                (int) ( getBlue() - right.getValue() ) );
    }
    else
    {
      final ConstantColor color = (ConstantColor) right;
      return new ConstantColor( getRed() - color.getRed(),
                                getGreen() - color.getGreen(),
                                getBlue() - color.getBlue() );
    }
  }

  public ConstantValue multiply( final ConstantValue right )
  {
    checkUnits( right );
    return new ConstantColor( (int) ( getRed() * right.getValue() ),
                              (int) ( getGreen() * right.getValue() ),
                              (int) ( getBlue() * right.getValue() ) );
  }

  public ConstantValue divide( final ConstantValue right )
  {
    checkUnits( right );
    if ( right.getValue() == 0.0 )
    {
      throw new DivideByZeroException();
    }
    return new ConstantColor( (int) ( getRed() / right.getValue() ),
                              (int) ( getGreen() / right.getValue() ),
                              (int) ( getBlue() / right.getValue() ) );
  }

  @Override
  public String toString()
  {
    return toCss( _compressed );
  }

  public String toCss( final boolean compress )
  {
    final String spacer = compress ? "" : " ";
    if ( !getAlpha().equals( MAX_ALPHA_VALUE ) )
    {
      final DecimalFormat alphaFormat = new DecimalFormat( "0.###" );
      return "rgba(" +
             getRed() + ',' + spacer +
             getGreen() + ',' + spacer +
             getBlue() + ',' + spacer +
             alphaFormat.format( getAlpha() ) + ')';
    }

    // Shorten colors of the form #aabbcc to #abc
    final String rs = Integer.toHexString( getRed() );
    final String gs = Integer.toHexString( getGreen() );
    final String bs = Integer.toHexString( getBlue() );

    final int r = getRed();
    final int g = getGreen();
    final int b = getBlue();

    if ( compress &&
         ( ( r & 0xf0 ) >> 4 ) == ( r & 0xf ) &&
         ( ( g & 0xf0 ) >> 4 ) == ( g & 0xf ) &&
         ( ( b & 0xf0 ) >> 4 ) == ( b & 0xf ) )
    {
      return "#" + rs.charAt( 0 ) + gs.charAt( 0 ) + bs.charAt( 0 );
    }
    else
    {
      // String.format("#%06x", (int) getValue()) would do the same thing, but this is much, much faster
      final StringBuilder buf = new StringBuilder( "#" );
      appendColorStr( buf, rs );
      appendColorStr( buf, gs );
      appendColorStr( buf, bs );
      return buf.toString();
    }
  }

  protected void appendColorStr( final StringBuilder buf, final String val )
  {
    if ( val.length() == 1 )
    {
      buf.append( '0' );
    }
    buf.append( val );
  }

  @Override
  public boolean equals( final Object obj )
  {
    if ( this == obj )
    {
      return true;
    }
    if ( null == obj || getClass() != obj.getClass() )
    {
      return false;
    }

    final ConstantColor that = (ConstantColor) obj;

    return this.getRed() == that.getRed() &&
           this.getGreen() == that.getGreen() &&
           this.getBlue() == that.getBlue() &&
           ( this.getAlpha().equals( that.getAlpha() ) );
  }

  @Override
  public int hashCode()
  {
    return (int) getValue();
  }

  protected int pinColor( final int value )
  {
    return Math.max( 0, Math.min( value, 0xff ) );
  }

  public int getRed()
  {
    return _red;
  }

  public void setRed( final int red )
  {
    _red = pinColor( red );
  }

  public int getGreen()
  {
    return _green;
  }

  public void setGreen( final int green )
  {
    _green = pinColor( green );
  }

  public int getBlue()
  {
    return _blue;
  }

  public void setBlue( final int blue )
  {
    _blue = pinColor( blue );
  }

  @Nonnull
  public BigDecimal getAlpha()
  {
    return _alpha;
  }

  public void setAlpha( @Nullable final BigDecimal alpha )
  {
    if ( null != alpha )
    {
      _alpha = alpha.min( MAX_ALPHA_VALUE ).max( MIN_ALPHA_VALUE );
    }
    else
    {
      _alpha = MAX_ALPHA_VALUE;
    }
  }

  public static boolean isColorFunction( final String value )
  {
    return isRGBFunction( value ) || isHSLFunction( value );
  }

  public static boolean isRGBFunction( final String value )
  {
    return value.length() >= 3 &&
           ( value.charAt( 0 ) == 'r' || value.charAt( 0 ) == 'R' ) &&
           ( value.charAt( 1 ) == 'g' || value.charAt( 1 ) == 'G' ) &&
           ( value.charAt( 2 ) == 'b' || value.charAt( 2 ) == 'B' );
  }

  public static boolean isHSLFunction( final String value )
  {
    return value.length() >= 3 &&
           ( value.charAt( 0 ) == 'h' || value.charAt( 0 ) == 'H' ) &&
           ( value.charAt( 1 ) == 's' || value.charAt( 1 ) == 'S' ) &&
           ( value.charAt( 2 ) == 'l' || value.charAt( 2 ) == 'L' );
  }

  public ConstantColor toARGB()
  {
    final ConstantColor constantColor =
      new ConstantColor( round( getAlpha().multiply( MAX_COLOR_COMPONENT_VALUE ) ), getRed(), getGreen() );
    constantColor.setAlpha( new BigDecimal( getBlue() ).divide( MAX_COLOR_COMPONENT_VALUE ) );
    return constantColor;
  }

  @Override
  public ConstantColor clone()
  {
    return new ConstantColor( this );
  }
}
