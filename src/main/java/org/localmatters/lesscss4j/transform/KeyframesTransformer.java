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
import org.localmatters.lesscss4j.model.BodyElement;
import org.localmatters.lesscss4j.model.Keyframes;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class KeyframesTransformer extends AbstractTransformer<Keyframes> {
    public KeyframesTransformer( final TransformerManager transformerManager) {
        super(transformerManager);
    }

    public KeyframesTransformer() {
    }

    public List<Keyframes> transform( final Keyframes keyframes, final EvaluationContext context) {
        final Keyframes transformed = new Keyframes(keyframes, false);
        evaluateVariables(keyframes, transformed, context);
        transformBodyElements(keyframes, transformed, context);
        return Arrays.asList(transformed);
    }

    protected void transformBodyElements( final Keyframes keyframes, final Keyframes transformed, final EvaluationContext context) {
        final EvaluationContext mediaContext = new EvaluationContext();
        mediaContext.setParentContext(context);
        mediaContext.setVariableContainer(transformed);
        mediaContext.setRuleSetContainer(transformed);

        for ( final BodyElement element : keyframes.getBodyElements()) {
            if (element instanceof RuleSet) {
                final RuleSet ruleSet = (RuleSet) element;
                final List<RuleSet> transformedRuleSets = getTransformer(ruleSet).transform(ruleSet, mediaContext);
                if (transformedRuleSets != null) {
                    for ( final RuleSet transformedRuleSet : transformedRuleSets) {
                        transformed.addBodyElement(transformedRuleSet);
                    }
                }
            }
            else {
                throw new IllegalStateException(
                    "Unexpected body element " + element.getClass().getSimpleName() + " in Media");
            }
        }
    }
}
