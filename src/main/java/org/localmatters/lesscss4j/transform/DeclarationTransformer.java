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
import org.localmatters.lesscss4j.error.ErrorUtils;
import org.localmatters.lesscss4j.error.LessCssException;
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.PositionAware;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.transform.manager.TransformerManager;

public class DeclarationTransformer extends AbstractTransformer<Declaration> {

    public DeclarationTransformer() {
    }

    public DeclarationTransformer( final TransformerManager transformerManager) {
        super(transformerManager);
    }

    public List<Declaration> transform( final Declaration declaration, final EvaluationContext context) {
        if (declaration.getValues() == null) return null;

        final Declaration transformed = new Declaration(declaration, false);
      for ( Object value : declaration.getValues() )
      {
        value = transformDeclarationValue( value, declaration, context );
        transformed.addValue( value );
      }

        return Arrays.asList(transformed);
    }

    protected Object transformDeclarationValue(Object value, final Declaration declaration, final EvaluationContext context) {
        if (value instanceof Expression) {
            try {
                Expression expression = (Expression) value;
                final Transformer<Expression> expressionTransformer = getTransformer(expression, false);
                if (expressionTransformer != null) {
                    // Can't think of a reason why we'd ever want to return more than one expression.
                    expression = expressionTransformer.transform(expression, context).get(0);
                }
                value = expression.evaluate(context);
            }
            catch ( final LessCssException ex) {
                ErrorUtils.handleError(context.getErrorHandler(), (PositionAware) value, ex);
            }
        }
        return value;
    }
}
