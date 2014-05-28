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

import org.localmatters.lesscss4j.error.UnitMismatchException;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class ConstantNumberTest
{
  protected void validateNumber( final double expectedValue, final String expectedUnit, final ConstantNumber actual )
  {
    assertEquals( expectedValue, actual.getValue(), "Unexpected value" );
    assertEquals( expectedUnit, actual.getUnit(), "Unexpected unit" );
  }

  @Test
  public void ParseNoUnits()
  {
    validateNumber( 5.0, null, new ConstantNumber( "5" ) );
    validateNumber( 3.0, null, new ConstantNumber( "3.0" ) );
    validateNumber( 3.0, null, new ConstantNumber( "3." ) );
    validateNumber( -3.0, null, new ConstantNumber( "-3" ) );
    validateNumber( 0.5, null, new ConstantNumber( ".5" ) );
  }

  @Test
  public void ParseWithUnits()
  {
    validateNumber( 5.0, "px", new ConstantNumber( "5px" ) );
    validateNumber( 3.0, "%", new ConstantNumber( "3.0%" ) );
    validateNumber( 3.0, "em", new ConstantNumber( "3.em" ) );
    validateNumber( -5.0, "px", new ConstantNumber( "-5px" ) );
    validateNumber( 0.5, "pt", new ConstantNumber( "0.5pt" ) );
    validateNumber( 0, "ms", new ConstantNumber( "0ms" ) );
    validateNumber( 0, "ms", new ConstantNumber( "0.0ms" ) );
  }

  @Test
  public void ToString()
  {
    assertEquals( "3", new ConstantNumber( 3, null ).toString() );
    assertEquals( "3.1em", new ConstantNumber( 3.1, "em" ).toString() );
    assertEquals( ".5px", new ConstantNumber( 0.5, "px" ).toString() );
    assertEquals( ".556px", new ConstantNumber( 0.5556, "px" ).toString() );
    assertEquals( "12.5px", new ConstantNumber( 12.5, "px" ).toString() );
    assertEquals( "3.01px", new ConstantNumber( 3.0101, "px" ).toString() );
    assertEquals( "0", new ConstantNumber( 0.0, null ).toString() );
    assertEquals( "0", new ConstantNumber( 0.0, "px" ).toString() );
    assertEquals( "0", new ConstantNumber( -0.0, "px" ).toString() );
    assertEquals( "-3.1em", new ConstantNumber( -3.1, "em" ).toString() );
  }

/*
    @Test public void ToStringPerformance() {
        Random random = new Random();
        double[] values = new double[100];
        for (int idx = 0; idx < values.length; idx++) {
            values[idx] = random.nextDouble() * 10;
        }

        StopWatch timer = new StopWatch();
        timer.setKeepTaskList(true);
        timer.start("DecimalFormat");
        perfTest(values, true);
        timer.stop();

        timer.start("Double.toString + cleanup");
        perfTest(values, false);
        timer.stop();

        System.out.println(timer.prettyPrint());
    }

    protected void perfTest(double[] values, boolean decimalFormat) {
        int ITERATIONS = 10000;
        for (int idx = 0; idx < ITERATIONS; idx++) {
            for (double value : values) {
                new ConstantNumber(value, null).toString(decimalFormat);
            }
        }
    }
*/

  @Test
  public void Add()
  {
    assertEquals( new ConstantNumber( 5, "px" ), new ConstantNumber( 3, null ).add( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 5, "px" ), new ConstantNumber( 3, "px" ).add( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 5, "px" ), new ConstantNumber( 3, "px" ).add( new ConstantNumber( 2, null ) ) );
    assertEquals( new ConstantNumber( 5, null ), new ConstantNumber( 3, null ).add( new ConstantNumber( 2, null ) ) );
  }

  @Test
  public void AddUnitMismatch()
  {
    try
    {
      new ConstantNumber( 3, "px" ).add( new ConstantNumber( 2, "em" ) );
      fail( "expected UnitMismatchException" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void AddColor()
  {
    try
    {
      new ConstantNumber( 3, "px" ).add( new ConstantColor( 0xffffff ) );
      fail( "expected exception" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void Subtract()
  {
    assertEquals( new ConstantNumber( 1, "px" ),
                  new ConstantNumber( 3, null ).subtract( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 1, "px" ),
                  new ConstantNumber( 3, "px" ).subtract( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 1, "px" ),
                  new ConstantNumber( 3, "px" ).subtract( new ConstantNumber( 2, null ) ) );
    assertEquals( new ConstantNumber( 1, null ),
                  new ConstantNumber( 3, null ).subtract( new ConstantNumber( 2, null ) ) );
  }

  @Test
  public void SubtractUnitMismatch()
  {
    try
    {
      new ConstantNumber( 3, "px" ).subtract( new ConstantNumber( 2, "em" ) );
      fail( "expected UnitMismatchException" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void SubtractColor()
  {
    try
    {
      new ConstantNumber( 3, "px" ).subtract( new ConstantColor( 0xffffff ) );
      fail( "expected exception" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void Divide()
  {
    assertEquals( new ConstantNumber( 1.5, "px" ),
                  new ConstantNumber( 3, null ).divide( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 1.5, "px" ),
                  new ConstantNumber( 3, "px" ).divide( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 1.5, "px" ),
                  new ConstantNumber( 3, "px" ).divide( new ConstantNumber( 2, null ) ) );
    assertEquals( new ConstantNumber( 1.5, null ),
                  new ConstantNumber( 3, null ).divide( new ConstantNumber( 2, null ) ) );
  }

  @Test
  public void DivideUnitMismatch()
  {
    try
    {
      new ConstantNumber( 3, "px" ).divide( new ConstantNumber( 2, "em" ) );
      fail( "expected UnitMismatchException" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void DivideColor()
  {
    try
    {
      new ConstantNumber( 3, "px" ).divide( new ConstantColor( 0xffffff ) );
      fail( "expected exception" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void Multiply()
  {
    assertEquals( new ConstantNumber( 6, "px" ),
                  new ConstantNumber( 3, null ).multiply( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 6, "px" ),
                  new ConstantNumber( 3, "px" ).multiply( new ConstantNumber( 2, "px" ) ) );
    assertEquals( new ConstantNumber( 6, "px" ),
                  new ConstantNumber( 3, "px" ).multiply( new ConstantNumber( 2, null ) ) );
    assertEquals( new ConstantNumber( 6, null ),
                  new ConstantNumber( 3, null ).multiply( new ConstantNumber( 2, null ) ) );
  }

  @Test
  public void MultiplyUnitMismatch()
  {
    try
    {
      new ConstantNumber( 3, "px" ).multiply( new ConstantNumber( 2, "em" ) );
      fail( "expected UnitMismatchException" );
    }
    catch ( final UnitMismatchException ex )
    {
      // expected
    }
  }

  @Test
  public void MultiplyColor()
  {
    assertEquals( new ConstantColor( 0x333333 ),
                  new ConstantNumber( 3, null ).multiply( new ConstantColor( 0x111111 ) ) );
    assertEquals( new ConstantColor( 0xffffff ),
                  new ConstantNumber( 3, null ).multiply( new ConstantColor( 0xaaaaaa ) ) );
  }
}
