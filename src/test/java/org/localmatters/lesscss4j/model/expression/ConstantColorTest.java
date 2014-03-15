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
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstantColorTest
{
  protected void validateColor( final int expectedValue, final ConstantColor actual )
  {
    validateColor( expectedValue, (Float) null, actual );
  }

  protected void validateColor( final int expectedValue, final Float expectedAlpha, final ConstantColor actual )
  {
    assertEquals( actual.getAlpha(), new BigDecimal(  expectedAlpha ), "Unexpected alpha value" );
    assertEquals( Integer.toHexString( expectedValue ),
                  Integer.toHexString( (int) actual.getValue() ),
                  "Unexpected color value" );
  }

  protected void validateColor( final int expectedValue, final String expectedAlpha, final ConstantColor actual )
  {
    assertEquals( actual.getAlpha(), new BigDecimal(  expectedAlpha ), "Unexpected alpha value" );
    assertEquals( Integer.toHexString( expectedValue ),
                  Integer.toHexString( (int) actual.getValue() ),
                  "Unexpected color value" );
  }

  @Test
  public void parseRgbaWithNegative()
  {
    validateColor( 0x001FFF, "0.42", new ConstantColor( "rgba(-99.9, 31.4159, 321, 0.42)" ) );
  }

  @Test
  public void ParseRGB()
  {
    validateColor( 0xfed412, new ConstantColor( "#fed412" ) );
    validateColor( 0xffeedd, new ConstantColor( "#fed" ) );
    validateColor( 0xffeedd, new ConstantColor( "rgb( 255, 238, 221 )" ) );
    validateColor( 0xffffff, 0.4f, new ConstantColor( "rGBa(255, 255, 255, 0.4)" ) );
    validateColor( 0xff7f00, 0.4f, new ConstantColor( "Rgba(100%, 50%, 0%, 0.4)" ) );
  }

  @Test
  public void ParseHSL()
  {
    validateColor( 0xff0000, new ConstantColor( "hsl(0, 100%, 50%)" ) );
    validateColor( 0x00ff00, new ConstantColor( "hSl(120, 100%, 50%)" ) );
    validateColor( 0xef8f8f, 1.0f, new ConstantColor( "hsLa(0, 75%, 75%, 1.0)" ) );
    validateColor( 0x80609f, 0.5f, new ConstantColor( "HSLa(270, 25%, 50%, 0.5)" ) );
    validateColor( 0xbfbf40, new ConstantColor( "hsl(60, 50, 50)" ) );
    validateColor( 0x808080, new ConstantColor( "hsl(60, 0%, 50%)" ) );
    validateColor( 0x206020, new ConstantColor( "hsl(120, 50%, 25%)" ) );
  }

  @Test
  public void ParseClipping()
  {
    validateColor( 0xff00ff, new ConstantColor( "rgb(256, -10, 255)" ) );
    validateColor( 0xbfbf40, new ConstantColor( "hsl(420, 50, 50)" ) ); // same as hsl(60, 50, 50)
    validateColor( 0xff0000, new ConstantColor( "hsl(0, 200%, 50%)" ) );
  }

  @Test
  public void ToString()
  {
    assertEquals( "#000", new ConstantColor( 0x000000 ).toString() );
    assertEquals( "#ab12de", new ConstantColor( 0xab12de ).toString() );
    assertEquals( "#001234", new ConstantColor( 0x001234 ).toString() );
    assertEquals( "#010204", new ConstantColor( 0x010204 ).toString() );
    assertEquals( "#ab1", new ConstantColor( 0xaabb11 ).toString() );
    assertEquals( "#fed", new ConstantColor( "rgb( 255, 238, 221 )" ).toString() );
    assertEquals( "rgba(255,255,255,0.4)", new ConstantColor( "RGBA(255, 255, 255, 0.4)" ).toString() );
  }

  @Test
  public void Add()
  {
    assertEquals( new ConstantColor( 0x00ff00 ), new ConstantColor( 0x00ee00 ).add( new ConstantColor( 0x009900 ) ) );
  }
}
