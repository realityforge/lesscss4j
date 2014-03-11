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
package org.localmatters.lesscss4j.transform;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class CompositeTransformer<T>
  implements Transformer<T>
{
  private List<Transformer<T>> _transformers;

  public List<Transformer<T>> getTransformers()
  {
    return _transformers;
  }

  public void setTransformers( final List<Transformer<T>> transformers )
  {
    _transformers = transformers;
  }

  public List<T> transform( @Nonnull final T value,
                            @Nonnull final EvaluationContext context,
                            @Nonnull final TransformerManager transformerManager )
  {
    final List<T> transformed = new ArrayList<>();
    transformed.add( value );

    for ( final Transformer<T> transformer : getTransformers() )
    {
      for ( int i = 0; i < transformed.size(); i++ )
      {
        final T val = transformed.get( i );
        final List<T> result = transformer.transform( val, context, transformerManager );
        if ( null != result && result.size() > 0 )
        {
          transformed.set( i, result.get( 0 ) );
          if ( result.size() > 1 )
          {
            for ( int j = 1; j < result.size(); j++ )
            {
              transformed.add( i + j, result.get( j ) );
            }
            i += result.size() - 1;
          }
        }
      }
    }
    return transformed;
  }
}
