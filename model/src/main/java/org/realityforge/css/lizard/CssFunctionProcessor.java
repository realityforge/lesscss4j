package org.realityforge.css.lizard;

import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import org.realityforge.css.lizard.CssFunction;

/**
 * Processor for CssFunction.
 */
@SupportedAnnotationTypes( "org.realityforge.css.lizard.CssFunction" )
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class CssFunctionProcessor
  extends AbstractProcessor
{
  @Override
  public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv )
  {

    if ( !roundEnv.processingOver() )
    {
      final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith( CssFunction.class );
      for ( final Element element : elements )
      {
        final CssFunction annotation = element.getAnnotation( CssFunction.class );
        final ExecutableElement ee = (ExecutableElement) element;
        final String name = annotation.name().isEmpty() ? ee.getSimpleName().toString() : annotation.name();
        super.processingEnv.getMessager().printMessage( Kind.NOTE, "CssFunction ahoy! " + name );
      }
    }
    return false;
  }
}
