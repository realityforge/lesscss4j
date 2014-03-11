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
import org.localmatters.lesscss4j.model.BodyElement;
import org.localmatters.lesscss4j.model.Media;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class MediaTransformer
  extends AbstractTransformer<Media>
{
  public List<Media> transform( @Nonnull final Media media,
                                @Nonnull final EvaluationContext context,
                                @Nonnull final TransformerManager transformerManager )
  {
    final Media transformed = new Media( media, false );
    evaluateVariables( media, transformed, context );
    transformBodyElements( media, transformed, context, transformerManager );
    return Arrays.asList( transformed );
  }

  protected void transformBodyElements( @Nonnull final Media media,
                                        @Nonnull final Media transformed,
                                        @Nonnull final EvaluationContext context,
                                        @Nonnull final TransformerManager transformerManager )
  {
    final EvaluationContext mediaContext = new EvaluationContext();
    mediaContext.setParentContext( context );
    mediaContext.setVariableContainer( transformed );
    mediaContext.setRuleSetContainer( transformed );

    for ( final BodyElement element : media.getBodyElements() )
    {
      if ( element instanceof RuleSet )
      {
        final RuleSet ruleSet = (RuleSet) element;
        final List<RuleSet> transformedRuleSets = performTransform( ruleSet, mediaContext, transformerManager );
        if ( null != transformedRuleSets )
        {
          for ( final RuleSet transformedRuleSet : transformedRuleSets )
          {
            transformed.addBodyElement( transformedRuleSet );
          }
        }
      }
      else
      {
        throw new IllegalStateException(
          "Unexpected body element " + element.getClass().getSimpleName() + " in Media" );
      }
    }
  }
}
