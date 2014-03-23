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
package org.localmatters.lesscss4j.model.expression;

import java.util.List;
import org.localmatters.lesscss4j.model.AbstractElement;
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.Selector;
import org.localmatters.lesscss4j.transform.EvaluationContext;

public class AccessorExpression
  extends AbstractElement
  implements Expression
{
  private Selector _selector;
  private String _property;
  private boolean _variable;

  public AccessorExpression()
  {
  }

  public AccessorExpression( final AccessorExpression copy )
  {
    super( copy );
    _selector = copy._selector.clone();
    _property = copy._property;
    _variable = copy._variable;
  }

  public Selector getSelector()
  {
    return _selector;
  }

  public void setSelector( final Selector selector )
  {
    _selector = selector;
  }

  public String getProperty()
  {
    return _property;
  }

  public void setProperty( final String property )
  {
    _property = property;
  }

  public boolean isVariable()
  {
    return _variable;
  }

  public void setVariable( final boolean variable )
  {
    _variable = variable;
  }

  public Expression evaluate( final EvaluationContext context )
  {
    final List<RuleSet> ruleSetList = context.getRuleSet( getSelector() );
    if ( null == ruleSetList || 0 == ruleSetList.size() )
    {
      // todo: error
    }
    else if ( ruleSetList.size() > 1 )
    {
      // todo: error
    }
    else
    {
      final RuleSet ruleSet = ruleSetList.get( 0 );
      if ( isVariable() )
      {
        final Expression var = ruleSet.getVariable( getProperty() );
        if ( null == var )
        {
          // todo: error
        }
        else
        {
          return var.evaluate( context );
        }
      }
      else
      {
        final Declaration declaration = ruleSet.getDeclaration( getProperty() );
        if ( null == declaration )
        {
          // todo: error
        }
        else
        {
          return new LiteralExpression( declaration.getValuesAsString() );
        }
      }
    }
    return this;
  }

  public AccessorExpression clone()
  {
    return new AccessorExpression( this );
  }
}
