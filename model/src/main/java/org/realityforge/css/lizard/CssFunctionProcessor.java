package org.realityforge.css.lizard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

/**
 * Processor for CssFunction.
 */
@SupportedAnnotationTypes( "org.realityforge.css.lizard.CssFunction" )
@SupportedSourceVersion( SourceVersion.RELEASE_7 )
public class CssFunctionProcessor
  extends AbstractProcessor
{
  private static final String CLASS_SUFFIX = "$CssFunctionMapper";
  private final HashSet<String> _classesProcessed = new HashSet<>();

  @Override
  public boolean process( final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv )
  {
    if ( !roundEnv.processingOver() )
    {
      final Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith( CssFunction.class );
      for ( final Element element : elements )
      {
        final ExecutableElement ee = (ExecutableElement) element;
        final Element parent = ee.getEnclosingElement();
        final ElementKind kind = parent.getKind();
        if ( ElementKind.CLASS == kind || ElementKind.INTERFACE == kind )
        {
          final TypeElement type = (TypeElement) parent;
          final String classname = type.getQualifiedName().toString();
          if ( !_classesProcessed.contains( classname ) )
          {
            try
            {
              processType( type );
              _classesProcessed.add( classname );
            }
            catch ( final IOException e )
            {
              final String error = toErrorString( e );
              final String message = "Error generating source for " + classname + ". Due to: " + error;
              processingEnv.getMessager().printMessage( Kind.ERROR, message );
            }
          }
        }
      }
    }
    return false;
  }

  private String toErrorString( final IOException e )
  {
    try ( final StringWriter stringWriter = new StringWriter() )
    {
      e.printStackTrace( new PrintWriter( stringWriter ) );
      return stringWriter.toString();
    }
    catch ( final IOException ioe )
    {
      return e.toString();
    }
  }

  private void processType( final TypeElement type )
    throws IOException
  {
    final String classname = type.getQualifiedName().toString();
    final StringBuilder sb = new StringBuilder();

    final int lastDot = classname.lastIndexOf( '.' );
    if ( -1 != lastDot )
    {
      sb.append( "package " );
      sb.append( classname.substring( 0, lastDot ) );
      sb.append( ";\n\n" );
    }

    sb.append( "public final class " );
    sb.append( type.getSimpleName().toString() );
    sb.append( CLASS_SUFFIX );
    sb.append( "\n" );
    sb.append( "{\n" );
    sb.append( "  private final " );
    sb.append( classname );
    sb.append( " _delegate;\n" );
    sb.append( "\n" );
    sb.append( "  public " );
    sb.append( type.getSimpleName().toString() );
    sb.append( CLASS_SUFFIX );
    sb.append( "( final " );
    sb.append( classname );
    sb.append( " delegate )\n" );

    sb.append( "  {\n" );
    sb.append( "    _delegate = delegate;\n" );
    for ( final Element element : type.getEnclosedElements() )
    {
      if ( ElementKind.METHOD == element.getKind() )
      {
        final CssFunction annotation = element.getAnnotation( CssFunction.class );
        if ( null != annotation )
        {
          final ExecutableElement ee = (ExecutableElement) element;
          if( ee.getModifiers().contains( Modifier.PRIVATE ) )
          {
            throw new IllegalStateException( "Annotation of the method " + ee.getSimpleName() +
                                             " on " + classname + " is invalid as the method is private. " );
          }
          final String name = annotation.name().isEmpty() ? ee.getSimpleName().toString() : annotation.name();
          processingEnv.getMessager().printMessage( Kind.NOTE, "CssFunction ahoy! " + name );
          sb.append( "    //css function " );
          sb.append( name );
          sb.append( "\n" );
        }
      }
    }
    sb.append( "  }\n" );
    sb.append( "\n}\n" );

    final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile( classname + CLASS_SUFFIX, type );
    try ( final Writer writer = sourceFile.openWriter() )
    {
      writer.write( sb.toString() );
    }
  }
}
