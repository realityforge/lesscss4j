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
package org.localmatters.lesscss4j.transform.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import org.localmatters.lesscss4j.transform.Transformer;

/**
 * Implementation of the {@link TransformerManager} that looks up {@link Transformer} instances based on the class of
 * the provided object.  Instances of this class are provided with a <code>Map</code> that provides a mapping between
 * <code>Class</code> and {@link Transformer} instances.  The {@link Transformer} to use for a particular object is
 * determined as follows:
 * <p/>
 * <ol>
 * <li>If there is a <code>Class</code> in the given Map that matches the given object's <code>Class</code> exactly,
 * the associated {@link Transformer} is returned.</li>
 * <li>Iterate over the entries in the given <code>Map</code>.  For the first entry where the <code>Class</code> is a
 * superclass or interface implemented by the given object, the associated {@link Transformer} is returned.</li>
 * </ol>
 * <p/>
 * As a result, it is highly recommended that the map provided to instances of this class return values from the
 * <code>entrySet</code> method in a consistent way. (i.e. something like <code>LinkedHashMap</code>)
 */
public class ClassTransformerManager
  implements TransformerManager
{
  @Nonnull
  private final Map<Class, Transformer> _classTransformerMap;

  public ClassTransformerManager( @Nonnull final Map<Class, Transformer> classTransformerMap )
  {
    _classTransformerMap = Collections.unmodifiableMap( new HashMap<>( classTransformerMap ) );
  }

  /**
   * Find the transformer for the given object.  The algorithm used is described in the description of this class.
   *
   * @return The located transformer.  <code>null</code> if no matching transformer can be found.
   */
  public <T> Transformer<T> getTransformer( @Nonnull final T object )
  {
    final Class objClass = object.getClass();

    final Transformer<T> transformer = _classTransformerMap.get( objClass );
    if ( null != transformer )
    {
      return transformer;
    }
    else
    {
      for ( final Map.Entry<Class, Transformer> entry : _classTransformerMap.entrySet() )
      {
        if ( entry.getKey().isAssignableFrom( objClass ) )
        {
          return entry.getValue();
        }
      }
    }
    return null;
  }
}
