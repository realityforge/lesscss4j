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

import java.util.List;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.RuleSetContainer;
import org.localmatters.lesscss4j.model.Selector;
import org.localmatters.lesscss4j.model.VariableContainer;
import org.localmatters.lesscss4j.model.expression.Expression;

public class EvaluationContext
{
  private EvaluationContext _parentContext;
  private VariableContainer _variableContainer;
  private RuleSetContainer _ruleSetContainer;
  private ErrorHandler _errorHandler;

  public EvaluationContext()
  {
    this( null );
  }

  public EvaluationContext( final VariableContainer variableContainer )
  {
    this( variableContainer, null );
  }

  public EvaluationContext( final VariableContainer variableContainer, final EvaluationContext parent )
  {
    setParentContext( parent );
    setVariableContainer( variableContainer );
  }

  public EvaluationContext getParentContext()
  {
    return _parentContext;
  }

  public void setParentContext( final EvaluationContext parentContext )
  {
    _parentContext = parentContext;
  }

  public VariableContainer getVariableContainer()
  {
    return _variableContainer;
  }

  public void setVariableContainer( final VariableContainer variableContainer )
  {
    _variableContainer = variableContainer;
  }

  public RuleSetContainer getRuleSetContainer()
  {
    return _ruleSetContainer;
  }

  public void setRuleSetContainer( final RuleSetContainer ruleSetContainer )
  {
    _ruleSetContainer = ruleSetContainer;
  }

  public Expression getVariable( final String name )
  {
    Expression value = null;
    if ( null != getVariableContainer() )
    {
      value = getVariableContainer().getVariable( name );
    }
    if ( null == value && null != getParentContext() )
    {
      value = getParentContext().getVariable( name );
    }
    return value;
  }

  public List<RuleSet> getRuleSet( final Selector selector )
  {
    List<RuleSet> ruleSet = null;
    if ( null != getRuleSetContainer() )
    {
      ruleSet = getRuleSetContainer().getRuleSet( selector );
    }
    if ( ( null == ruleSet || 0 == ruleSet.size() ) && null != getParentContext() )
    {
      ruleSet = getParentContext().getRuleSet( selector );
    }
    return ruleSet;
  }

  public ErrorHandler getErrorHandler()
  {
    if ( null != _errorHandler )
    {
      return _errorHandler;
    }
    else if ( null != getParentContext() )
    {
      return getParentContext().getErrorHandler();
    }
    else
    {
      return null;
    }
  }

  public void setErrorHandler( final ErrorHandler errorHandler )
  {
    _errorHandler = errorHandler;
  }
}
