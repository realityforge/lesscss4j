package org.realityforge.css.lizard.model;

import java.math.BigDecimal;
import javax.annotation.Nonnull;

public final class NumberValue
{
  @Nonnull
  private final BigDecimal _value;

  public NumberValue( final BigDecimal value )
  {
    _value = value;
  }

  @Nonnull
  public BigDecimal getValue()
  {
    return _value;
  }

  @Override
  public String toString()
  {
    return _value.toPlainString();
  }
}
