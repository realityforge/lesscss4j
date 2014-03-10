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

public class Keyframes
  extends BodyElementContainer
  implements BodyElement
{
  private String _name;

  public Keyframes()
  {
  }


  public Keyframes( final Keyframes copy )
  {
    this( copy, true );
  }

  public Keyframes( final Keyframes copy, final boolean copyBodyElements )
  {
    super( copy, copyBodyElements );
    _name = copy._name;
  }

  public String getName()
  {
    return _name;
  }

  public void setName( final String name )
  {
    _name = name;
  }

  @Override
  public Object clone()
  {
    return new Keyframes( this );
  }

  @Override
  public String toString()
  {
    return getName();
  }
}
