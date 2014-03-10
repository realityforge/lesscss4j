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
package org.localmatters.lesscss4j.model;

import java.util.ArrayList;
import java.util.List;
import org.localmatters.lesscss4j.model.expression.Expression;

public class Declaration
  extends AbstractElement
  implements DeclarationElement
{
  private String _property;
  private List<Object> _values;
  private boolean _important = false;

  public Declaration()
  {
  }

  public Declaration( final Declaration copy )
  {
    this( copy, true );
  }

  public Declaration( final Declaration copy, final boolean copyValues )
  {
    _property = copy._property;
    _important = copy._important;
    if ( copyValues && null != copy._values )
    {
      _values = new ArrayList<>( copy._values.size() );
      for ( final Object value : copy._values )
      {
        if ( value instanceof Expression )
        {
          _values.add( ( (Expression) value ).clone() );
        }
        else
        {
          _values.add( value );
        }
      }
    }
  }

  public String getProperty()
  {
    return _property;
  }

  public void setProperty( final String property )
  {
    _property = property;
  }

  public List<Object> getValues()
  {
    return _values;
  }

  public void setValues( final List<Object> values )
  {
    _values = values;
  }

  public void addValue( final Object value )
  {
    if ( null == _values )
    {
      _values = new ArrayList<>();
    }
    _values.add( value );
  }

  public boolean isImportant()
  {
    return _important;
  }

  public void setImportant( final boolean important )
  {
    _important = important;
  }

  public String getValuesAsString()
  {
    return getValuesAsString( new StringBuilder() );
  }

  public String getValuesAsString( final StringBuilder buf )
  {
    for ( final Object value : getValues() )
    {
      buf.append( value.toString() );
    }
    return buf.toString();
  }

  @Override
  public String toString()
  {
    final StringBuilder buf = new StringBuilder();
    buf.append( getProperty() );
    buf.append( ": " );
    getValuesAsString( buf );
    if ( isImportant() )
    {
      buf.append( " !important" );
    }
    return buf.toString();
  }

  @Override
  public Declaration clone()
  {
    return new Declaration( this );
  }
}
