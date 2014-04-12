package org.realityforge.css.lizard.model;

public final class Percentage
{
  private final NumberValue _number;

  public Percentage( final NumberValue number )
  {
    _number = number;
  }

  public NumberValue getNumber()
  {
    return _number;
  }

  @Override
  public String toString()
  {
    return _number.toString() + "%";
  }
}
