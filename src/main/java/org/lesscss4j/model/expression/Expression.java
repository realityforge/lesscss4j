/**
 * File: Expression.java
 *
 * Author: David Hay (dhay@localmatters.com)
 * Creation Date: Apr 22, 2010
 * Creation Time: 4:04:53 PM
 *
 * Copyright 2010 Local Matters, Inc.
 * All Rights Reserved
 *
 * Last checkin:
 *  $Author$
 *  $Revision$
 *  $Date$
 */
package org.lesscss4j.model.expression;

import org.lesscss4j.model.PositionAware;
import org.lesscss4j.transform.EvaluationContext;

public interface Expression extends PositionAware {
    Expression evaluate(EvaluationContext context);
}
