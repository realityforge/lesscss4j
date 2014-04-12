package org.realityforge.css.lizard.model;

import java.math.BigDecimal;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class PercentageValueTest
{
  @Test
  public void string()
  {
    assertSame( "90", "90%" );
    assertSame( "-1.00", "-1.00%" );
    assertSame( "1", "1%" );
    assertSame( "100", "100%" );
  }

  private void assertSame( final String input, final String expected )
  {
    assertEquals( new Percentage( new NumberValue( new BigDecimal( input ) ) ).toString(), expected );
  }
}
