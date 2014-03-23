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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DeclarationContainer
  extends BodyElementContainer
{
  private final Map<String, Declaration> _declarationMap = new LinkedHashMap<>();
  private final List<DeclarationElement> _declarations = new ArrayList<>();
  private boolean _mixinReferenceUsed = false;

  public DeclarationContainer()
  {
  }

  public DeclarationContainer( final DeclarationContainer copy )
  {
    this( copy, true );
  }

  public DeclarationContainer( final DeclarationContainer copy, final boolean copyDeclarations )
  {
    super( copy );
    if ( copyDeclarations )
    {
      for ( final DeclarationElement declaration : copy._declarations )
      {
        addDeclaration( declaration.clone() );
      }
    }
  }

  public boolean isMixinReferenceUsed()
  {
    return _mixinReferenceUsed;
  }

  public List<DeclarationElement> getDeclarations()
  {
    return _declarations;
  }

  public void clearDeclarations()
  {
    _declarations.clear();
    _declarationMap.clear();
    _mixinReferenceUsed = false;
  }

  public void addDeclarations( final Collection<? extends DeclarationElement> declarations )
  {
    if ( null != declarations )
    {
      for ( final DeclarationElement declaration : declarations )
      {
        addDeclaration( declaration );
      }
    }
  }

  public void addDeclaration( final DeclarationElement declaration )
  {
    _declarations.add( declaration );
    addDeclarationMapEntry( declaration );

    if ( declaration instanceof MixinReference )
    {
      _mixinReferenceUsed = true;
    }
  }

  protected void addDeclarationMapEntry( final DeclarationElement declaration )
  {
    if ( declaration instanceof Declaration )
    {
      _declarationMap.put( ( (Declaration) declaration ).getProperty(), (Declaration) declaration );
    }
  }

  public Declaration getDeclaration( final String property )
  {
    return _declarationMap.get( property );
  }
}
