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

import org.localmatters.lesscss4j.error.DivideByZeroException;
import org.localmatters.lesscss4j.error.UnitMismatchException;

public class ConstantNumber
  implements ConstantValue
{
  private double _value;
  private String _unit;

  public ConstantNumber()
  {
    this( 0, null );
  }

  public ConstantNumber( final ConstantNumber copy )
  {
    _value = copy._value;
    _unit = copy._unit;
  }

  public ConstantNumber( final double value, final String unit )
  {
    setValue( value );
    setUnit( unit );
  }

  public ConstantNumber( String value )
  {
    value = ( null != value ) ? value.trim() : value;
    if ( null == value || 0 == value.length() )
    {
      setValue( 0 );
      setUnit( null );
    }
    else
    {
      int unitIdx = -1;
      for ( int numChars = value.length(), idx = numChars - 1; idx >= 0; idx-- )
      {
        final char ch = value.charAt( idx );
        if ( '0' <= ch && ch <= '9' || ch == '.' )
        {
          unitIdx = idx + 1;
          break;
        }
      }

      if ( unitIdx >= 0 )
      {
        setValue( Double.parseDouble( value.substring( 0, unitIdx ) ) );
        setUnit( value.substring( unitIdx ) );
      }
    }
  }

  public String getUnit()
  {
    return _unit;
  }

  public void setUnit( final String unit )
  {
    _unit = null != unit ? unit.trim() : null;
    if ( null != _unit && 0 == _unit.length() )
    {
      _unit = null;
    }
  }

  public void setValue( final double value )
  {
    _value = value;
  }

  public double getValue()
  {
    return _value;
  }

  protected boolean hasCompatibleUnits( final ConstantNumber that )
  {
    return null == this.getUnit() || null == that.getUnit() || this.getUnit().equals( that.getUnit() );
  }

  protected void checkUnits( final ConstantValue that )
  {
    if ( !( that instanceof ConstantNumber ) || !hasCompatibleUnits( (ConstantNumber) that ) )
    {
      throw new UnitMismatchException( this, that );
    }
  }

  protected String selectUnit( final ConstantValue right )
  {
    return null != getUnit() ? getUnit() : ( (ConstantNumber) right ).getUnit();
  }

  public ConstantValue add( final ConstantValue right )
  {
    checkUnits( right );
    return new ConstantNumber( this.getValue() + right.getValue(), selectUnit( right ) );
  }

  public ConstantValue subtract( final ConstantValue right )
  {
    checkUnits( right );
    return new ConstantNumber( this.getValue() - right.getValue(), selectUnit( right ) );
  }

  public ConstantValue multiply( final ConstantValue right )
  {
    if ( right instanceof ConstantColor && null == getUnit() )
    {
      return right.multiply( this );
    }
    else
    {
      checkUnits( right );
      return new ConstantNumber( this.getValue() * right.getValue(), selectUnit( right ) );
    }
  }

  public ConstantValue divide( final ConstantValue right )
  {
    checkUnits( right );
    if ( right.getValue() == 0.0 )
    {
      throw new DivideByZeroException();
    }
    return new ConstantNumber( this.getValue() / right.getValue(), selectUnit( right ) );
  }

  public static final int DECIMAL_PLACES = 3;
  public static final double ROUND_MULTIPLIER = Math.pow( 10.0, DECIMAL_PLACES );

  @Override
  public String toString()
  {
    // By using Double.toString and post-processing the result, this method takes only
    // 1/3 of the time it takes to do the same thing using DecimalFormat.format();
    double value = getValue();
    if ( ( value - ( (int) value ) * ROUND_MULTIPLIER ) > 0 )
    {
      value = Math.round( value * ROUND_MULTIPLIER ) / ROUND_MULTIPLIER;
    }
    final String str = Double.toString( value );

    final char[] chars = new char[ str.length() + ( null != getUnit() ? getUnit().length() : 0 ) ];
    boolean hasInt = false;
    int i = 0;
    boolean negative = false;
    for ( int c = 0; c < str.length(); c++ )
    {
      char ch = str.charAt( c );
      if ( '0' <= ch && ch <= '9' )
      {
        // Ignore leading zeros in the integer portion of the number
        if ( ch != '0' || hasInt )
        {
          if ( i == 0 && negative )
          {
            chars[ i++ ] = '-';
          }
          hasInt = true;
          chars[ i++ ] = ch;
        }
      }
      else if ( ch == '-' )
      {
        negative = true;
      }
      else if ( ch == '.' )
      {
        // Found decimal point...see if we have any non-zero values
        c++;

        final int decimalLength = Math.min( DECIMAL_PLACES, str.length() - c );

        // Find the last non-zero digit
        int lastNonZeroIdx = -1;
        for ( int j = decimalLength - 1; j >= 0; j-- )
        {
          ch = str.charAt( c + j );
          if ( ch != '0' )
          {
            lastNonZeroIdx = j;
            break;
          }
        }

        // Copy everything up to and including the last non-zero digit
        if ( lastNonZeroIdx >= 0 )
        {
          if ( i == 0 && negative )
          {
            chars[ i++ ] = '-';
          }
          chars[ i++ ] = '.';
          for ( int j = 0; j <= lastNonZeroIdx; j++ )
          {
            chars[ i++ ] = str.charAt( c + j );
          }
        }
        break;
      }
    }
    if ( i == 0 )
    {
      return "0";
    }
    else
    {
      final String unit = getUnit();
      if ( null != unit && 0.0 != value )
      {
        for ( int c = 0; c < unit.length(); c++ )
        {
          chars[ i++ ] = unit.charAt( c );
        }
      }
      return new String( chars, 0, i );
    }
/*
        }
        else {
            DecimalFormat format = new DecimalFormat("#.###" + (null != getUnit() "'" + getUnit() + "'" : ""));
            format.setMinimumIntegerDigits(0);
            format.setMinimumFractionDigits(0);
            return format.format(getValue());
        }
*/
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

    final ConstantNumber that = (ConstantNumber) obj;

    if ( Double.compare( that._value, _value ) != 0 )
    {
      return false;
    }
    return !( null != _unit ? !_unit.equals( that._unit ) : null != that._unit );
  }

  @Override
  public int hashCode()
  {
    int result;
    final long temp;
    temp = _value != +0.0d ? Double.doubleToLongBits( _value ) : 0L;
    result = (int) ( temp ^ ( temp >>> 32 ) );
    result = 31 * result + ( null != _unit ? _unit.hashCode() : 0 );
    return result;
  }

  public ConstantNumber clone()
  {
    return new ConstantNumber( this );
  }
}
