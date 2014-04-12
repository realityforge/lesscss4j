package org.realityforge.css.lizard.model;

import javax.annotation.Nonnull;

public final class Dimension
{
  @Nonnull
  private final NumberValue _number;
  @Nonnull
  private final String _unit;

  public Dimension( @Nonnull final NumberValue number, @Nonnull final String unit )
  {
    _number = number;
    _unit = unit;
  }

  @Nonnull
  public NumberValue getNumber()
  {
    return _number;
  }

  @Nonnull
  public String getUnit()
  {
    return _unit;
  }
}
