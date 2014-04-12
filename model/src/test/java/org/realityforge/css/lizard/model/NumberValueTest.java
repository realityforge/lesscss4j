package org.realityforge.css.lizard.model;

import java.math.BigDecimal;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class NumberValueTest
{
  @Test
  public void string()
  {
    assertSame( "1.00", "1.00" );
    assertSame( "-1.00", "-1.00" );
    assertSame( "1", "1" );
    assertSame( "1222.0000", "1222.0000" );
  }

  private void assertSame( final String input, final String expected )
  {
    assertEquals( new NumberValue( new BigDecimal( input ) ).toString(), expected );
  }
}
