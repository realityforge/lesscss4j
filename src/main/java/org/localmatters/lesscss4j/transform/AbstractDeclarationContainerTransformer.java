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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.localmatters.lesscss4j.error.ErrorUtils;
import org.localmatters.lesscss4j.error.LessCssException;
import org.localmatters.lesscss4j.error.MixinArgumentMismatchException;
import org.localmatters.lesscss4j.error.UndefinedMixinReference;
import org.localmatters.lesscss4j.model.BodyElement;
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.DeclarationContainer;
import org.localmatters.lesscss4j.model.DeclarationElement;
import org.localmatters.lesscss4j.model.MixinReference;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.Selector;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public abstract class AbstractDeclarationContainerTransformer<T extends DeclarationContainer>
    extends AbstractTransformer<T> {

    protected AbstractDeclarationContainerTransformer() {
    }

    protected AbstractDeclarationContainerTransformer( final TransformerManager transformerManager) {
        super(transformerManager);
    }

    protected void transformDeclarations( final T container, final T transformed, final EvaluationContext context) {

        if (!container.isMixinReferenceUsed()) {
            final EvaluationContext declContext = new EvaluationContext();
            declContext.setParentContext(context);
            declContext.setVariableContainer(container);
            declContext.setRuleSetContainer(container);

            for ( final DeclarationElement declaration : container.getDeclarations()) {
                if (declaration instanceof Declaration) {
                    transformed.addDeclarations(getTransformer(declaration).transform(declaration, declContext));
                }
                else {
                    // todo: error
                }
            }
        }
        else {
            transformDeclarationsWithMixins(container, transformed, context);
        }
    }

    private void transformDeclarationsWithMixins( final DeclarationContainer container,
                                                 final DeclarationContainer transformed,
                                                 final EvaluationContext context) {

        final EvaluationContext declContext = new EvaluationContext();
        declContext.setParentContext(context);
        declContext.setVariableContainer(transformed);
        declContext.setRuleSetContainer(transformed);


        // Mixins might define additional variables referenced by declaration values.
        // So we need to process all of the mixins before we transform the declarations.
        final List<Declaration> declarationList = new ArrayList<>(container.getDeclarations().size());
        for ( final DeclarationElement declaration : container.getDeclarations()) {
            if (declaration instanceof Declaration) {
                declarationList.add((Declaration) declaration);
            }
            else if (declaration instanceof MixinReference) {
                final MixinReference mixin = (MixinReference) declaration;
                try {
                    final Selector selector = mixin.getSelector();
                    final List<RuleSet> ruleSetList = context.getRuleSet(selector);
                    if (ruleSetList != null) {
                        for (RuleSet ruleSet : ruleSetList) {
                            ruleSet = ruleSet.clone();

                            updateMixinArguments(ruleSet, mixin);

                            final List<RuleSet> mixinRuleSets = getTransformer(ruleSet).transform(ruleSet, declContext);

                            ruleSet = mixinRuleSets.get(0);


                            for ( final Iterator<String> iter = ruleSet.getVariableNames(); iter.hasNext();) {
                                final String varName = iter.next();
                                final Expression expression = ruleSet.getVariable(varName);
                                transformed.setVariable(varName, expression);

                            }

                            for ( final DeclarationElement element : ruleSet.getDeclarations()) {
                                if (element instanceof Declaration) {
                                    declarationList.add((Declaration) element);
                                }
                            }

                            for ( final BodyElement bodyElement : ruleSet.getBodyElements()) {
                                // todo: check for collisions with selector names
                                transformed.addBodyElement(bodyElement);
                            }
                        }
                    }
                    else {
                        throw new UndefinedMixinReference(mixin);
                    }
                }
                catch ( final LessCssException ex) {
                    ErrorUtils.handleError(context.getErrorHandler(), mixin, ex);
                }
            }
        }

        for ( final Declaration declaration : declarationList) {
            transformed.addDeclarations(getTransformer(declaration).transform(declaration, declContext));
        }
    }

    /**
     * Updates the variables in the rule set with the values from the argument list.  The values are either the default
     * values defined in the mixin or the values specified in the mixin call argument list.
     *
     * @param ruleSet The rule set to update
     * @param mixin   The mixin reference containing the call arguments
     */
    protected void updateMixinArguments( final RuleSet ruleSet, final MixinReference mixin) {
        if (mixin.getArguments().size() > ruleSet.getArguments().size()) {
            throw new MixinArgumentMismatchException(mixin, ruleSet);
        }

        if (ruleSet.getArguments().size() > 0) {

            // If there are no call arguments, we're done.  The RuleSet had it's variable
            // map updated with the default argument values when it was constructed.
            if (mixin.getArguments().size() > 0) {
                final Iterator<Expression> callIter = mixin.getArguments().iterator();
                final Iterator<Map.Entry<String, Expression>> argIter = ruleSet.getArguments().entrySet().iterator();

                while (callIter.hasNext()) {
                    final Expression mixinValue = callIter.next();
                    final Map.Entry<String, Expression> entry = argIter.next();

                    ruleSet.setVariable(entry.getKey(), mixinValue);
                }
            }

            ruleSet.getArguments().clear();
        }
    }

    protected void transformRuleSets( final List<T> transformed, final EvaluationContext context) {
        final T container = transformed.get(0);
        if (container.getRuleSetCount() == 0) {
            return;
        }

        final EvaluationContext ruleSetContext = new EvaluationContext();
        ruleSetContext.setParentContext(context);
        ruleSetContext.setVariableContainer(container);
        ruleSetContext.setRuleSetContainer(container);

        // First transform all the child rule sets.
        final List<BodyElement> elements = container.getBodyElements();
        for ( final BodyElement element : elements) {
            if (element instanceof RuleSet) {
                final RuleSet childRuleSet = (RuleSet) element;
                final List<RuleSet> transformedList = getTransformer(childRuleSet).transform(childRuleSet, ruleSetContext);
                for ( final RuleSet transformedChild : transformedList) {
                    if (container instanceof RuleSet) {
                        updateChildSelectors((RuleSet) container, transformedChild);
                    }
                    transformed.add((T) transformedChild);
                }
            }
        }
    }

    protected void updateChildSelectors( final RuleSet parent, final RuleSet child) {
        final List<Selector> selectorList = new ArrayList<>();
        for ( final Selector parentSelector : parent.getSelectors()) {
          for ( final Selector childSelector : child.getSelectors() )
          {
            selectorList.add( new Selector( parentSelector, childSelector ) );
          }
        }

        child.setSelectors(selectorList);
    }

    public void doTransform( final T container, final List<T> transformed, final EvaluationContext context) {
        evaluateVariables(container, transformed.get(0), context);
        transformDeclarations(container, transformed.get(0), context);
        transformRuleSets(transformed, context);
    }
}
