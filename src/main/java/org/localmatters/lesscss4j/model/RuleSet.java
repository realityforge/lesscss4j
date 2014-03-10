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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.localmatters.lesscss4j.model.expression.Expression;


public class RuleSet extends DeclarationContainer implements BodyElement, Cloneable {
    private List<Selector> _selectors;
    private final Map<String, Expression> _arguments = new LinkedHashMap<>();

    public RuleSet() {
    }

    public RuleSet( final RuleSet copy) {
        this(copy, true);
    }

    public RuleSet( final RuleSet copy, final boolean copyDeclarations) {
        super(copy, copyDeclarations);
        if (copy._selectors != null) {
            _selectors = new ArrayList<>(copy._selectors.size());
            for ( final Selector selector : copy._selectors) {
                _selectors.add(selector.clone());
            }
        }

        for ( final Map.Entry<String, Expression> entry : copy._arguments.entrySet()) {
            _arguments.put(entry.getKey(), entry.getValue().clone());
        }
    }

    public List<Selector> getSelectors() {
        return _selectors;
    }

    public void setSelectors( final List<Selector> selectors) {
        _selectors = selectors;
    }

    public void addSelector( final Selector selector) {
        if (_selectors == null) {
            _selectors = new ArrayList<>();
        }
        _selectors.add(selector);
    }

    public Map<String, Expression> getArguments() {
        return _arguments;
    }

    public void addArgument( final String name, final Expression value) {
        _arguments.put(name, value);
    }

    @Override
    public RuleSet clone() {
        return new RuleSet(this);
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        for ( final Selector selector : _selectors) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(selector.toString());
        }
        return buf.toString();
    }
}
