package org.localmatters.lesscss4j.transform.function2;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation used to identify a function to be exported to the runtime.
 */
@Target( METHOD )
@Retention( RUNTIME )
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
