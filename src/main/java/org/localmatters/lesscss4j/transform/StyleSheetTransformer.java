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
import org.localmatters.lesscss4j.model.StyleSheet;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class StyleSheetTransformer extends AbstractTransformer<StyleSheet> {
    public StyleSheetTransformer() {
    }

    public StyleSheetTransformer( final TransformerManager transformerManager) {
        super(transformerManager);
    }

    public List<StyleSheet> transform( final StyleSheet styleSheet, final EvaluationContext context) {
        final StyleSheet transformed = new StyleSheet();

        processImports(styleSheet, transformed, context);
        evaluateVariables(styleSheet, transformed, context);

        transformBodyElements(styleSheet, transformed, context);

        return Arrays.asList(transformed);
    }

    protected void processImports( final StyleSheet styleSheet, final StyleSheet transformed, final EvaluationContext context) {
        // Imports are handled by the Parser.  Don't need to do anything since
        // we don't want to output any @import statements in the writer.
    }

    protected void transformBodyElements( final StyleSheet styleSheet, final StyleSheet transformed, final EvaluationContext context) {
        final EvaluationContext styleContext = new EvaluationContext();
        styleContext.setParentContext(context);
        styleContext.setVariableContainer(transformed);
        styleContext.setRuleSetContainer(transformed);

        final List<BodyElement> elements = styleSheet.getBodyElements();
        for ( final BodyElement element : elements) {
            final List<? extends BodyElement> transformedElementList = transformBodyElement(element, styleContext);
            if (transformedElementList != null) {
                for ( final BodyElement transformedElement : transformedElementList) {
                    transformed.addBodyElement(transformedElement);
                }
            }
        }
    }

    private List<? extends BodyElement> transformBodyElement( final BodyElement element, final EvaluationContext styleContext) {
        return getTransformer(element).transform(element, styleContext);
    }
}
