package org.realityforge.css.lizard;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to identify a function to be exported to the runtime.
 */
@Target( ElementType.METHOD )
@Retention( RetentionPolicy.RUNTIME )
@Documented
public @interface CssFunction
{
  /**
   * Return the name of the function.
   * If unspecified the name of the annotated method will be used.
   *
   * @return the name of the function.
   */
  String name() default "";

  /**
   * Return the description of the function.
   * Used as part of the documentation generation process.
   *
   * @return the description of the function.
   */
  String description() default "";

  /**
   * Return an example usage of the function.
   * If not specified the documentation generation process will attempt to generate
   * examples based on the types of the parameters.
   *
   * @return an example usage of the function.
   */
  String usage() default "";
}
