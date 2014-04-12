package org.realityforge.css.lizard.model;

import java.math.BigDecimal;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class DimensionValueTest
{
  @Test
  public void string()
  {
    assertSame( "90", "em", "90em" );
    assertSame( "-1.00", "px", "-1.00px" );
    assertSame( "1", "rem", "1rem" );
  }

  private void assertSame( final String input, final String dimension, final String expected )
  {
    assertEquals( new Dimension( new NumberValue( new BigDecimal( input ) ), dimension ).toString(), expected );
  }
}
