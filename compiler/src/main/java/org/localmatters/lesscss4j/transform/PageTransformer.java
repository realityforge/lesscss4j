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

import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;
import org.localmatters.lesscss4j.model.Page;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class PageTransformer
  extends AbstractDeclarationContainerTransformer<Page>
{
  public List<Page> transform( @Nonnull final Page page,
                               @Nonnull final EvaluationContext context,
                               @Nonnull final TransformerManager transformerManager )
  {
    final List<Page> pageList = Arrays.asList( new Page( page, false ) );
    doTransform( page, pageList, context, transformerManager );
    return pageList;
  }
}
