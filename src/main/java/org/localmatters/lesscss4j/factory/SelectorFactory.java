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
package org.localmatters.lesscss4j.factory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.tree.Tree;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.model.Selector;

public class SelectorFactory
  extends AbstractObjectFactory<Selector>
{
  @Nullable
  public Selector create( @Nonnull final Tree selectorNode, @Nullable final ErrorHandler errorHandler )
  {
    final String selectorText = concatChildNodeText( selectorNode );
    if ( selectorText.length() > 0 )
    {
      final Selector selector = new Selector( selectorText );
      selector.setLine( selectorNode.getLine() );
      selector.setChar( selectorNode.getCharPositionInLine() );
      return selector;
    }
    else
    {
      return null;
    }
  }
}

