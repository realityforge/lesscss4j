package org.realityforge.css.lizard;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import org.realityforge.css.lizard.model.Dimension;
import org.realityforge.css.lizard.model.NumberValue;
import org.realityforge.css.lizard.model.Percentage;

/**
 * Processor for CssFunction.
 */
@SupportedAnnotationTypes("org.realityforge.css.lizard.CssFunction")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class CssFunctionProcessor
  extends AbstractProcessor
{
  public static final String CLASS_SUFFIX = "$CssFunctionMapper";
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

    sb.append( "import java.math.BigDecimal;\n" );
    sb.append( "import java.util.Map;\n" );
    sb.append( "import java.util.HashMap;\n" );
    sb.append( "import org.localmatters.lesscss4j.error.FunctionException;\n" );
    sb.append( "import org.localmatters.lesscss4j.model.expression.Expression;\n" );
    sb.append( "import org.realityforge.css.lizard.model.NumberValue;\n" );
    sb.append( "import org.realityforge.css.lizard.model.Percentage;\n" );
    sb.append( "import org.realityforge.css.lizard.model.Dimension;\n" );
    sb.append( "import org.realityforge.css.lizard.model.ColorKeyword;\n" );
    sb.append( "import org.localmatters.lesscss4j.transform.function.AbstractFunction;\n" );
    sb.append( "import org.localmatters.lesscss4j.transform.function.Function;\n" );
    sb.append( "\n" );

    sb.append( "public final class " );
    sb.append( type.getSimpleName().toString() );
    sb.append( CLASS_SUFFIX );
    sb.append( "\n" );
    sb.append( "{\n" );
    sb.append( "\n" );
    sb.append( "  public static Map<String, Function> toFunctionMap( final " );
    sb.append( classname );
    sb.append( " delegate )\n" );

    sb.append( "  {\n" );
    sb.append( "    final Map<String, Function> functions = new HashMap<>();\n" );
    for ( final Element element : type.getEnclosedElements() )
    {
      if ( ElementKind.METHOD == element.getKind() )
      {
        final CssFunction annotation = element.getAnnotation( CssFunction.class );
        if ( null != annotation )
        {
          final ExecutableElement ee = (ExecutableElement) element;
          if ( ee.getModifiers().contains( Modifier.PRIVATE ) )
          {
            throw new IllegalStateException( "Annotation of the method " + ee.getSimpleName() +
                                             " on " + classname + " is invalid as the method is private. " );
          }
          final List<? extends VariableElement> parameters = ee.getParameters();
          final String name = annotation.name().isEmpty() ? ee.getSimpleName().toString() : annotation.name();
          processingEnv.getMessager().printMessage( Kind.NOTE, "CssFunction ahoy! " + name );
          sb.append( "    //css function " );
          sb.append( name );
          sb.append( "\n" );


          sb.append( "    final AbstractFunction " );
          sb.append( name );
          sb.append( " = new AbstractFunction() {\n" );
          sb.append( "      @Override\n" );
          sb.append(
            "      public Expression evaluate( final String name, final Expression... args )\n" );
          sb.append( "      {\n" );
          sb.append( "        if( " );
          final int parameterCount = parameters.size();
          sb.append( parameterCount );
          sb.append( " != args.length )\n" );
          sb.append( "        {\n" );
          sb.append( "          final String message = \"Expected " );
          sb.append( parameterCount );
          sb.append( " argument" );
          if( 1 < parameterCount )
          {
            sb.append( "s" );
          }
          sb.append( " for function '" );
          sb.append( name );
          sb.append( "' but passed %d\";\n" );
          sb.append( "          throw new FunctionException( message, args.length );\n" );
          sb.append( "        }\n" );

          final ArrayList<String> args = new ArrayList<>();
          for ( int i = 0; i < parameterCount; i++ )
          {
            final VariableElement parameter = parameters.get( i );
            final String parameterType = parameter.asType().toString();
            if( parameterType.equals( Percentage.class.getName() ) )
            {
              sb.append( "        if( !isPercentage( args[" );
              sb.append( i );
              sb.append( "] ) )\n" );
              sb.append( "        {\n" );
              sb.append( "          final String message = \"Argument " );
              sb.append( i + 1 );
              sb.append( " to function '"  );
              sb.append( name );
              sb.append( "' must be a percentage: %s\";\n" );
              sb.append( "          throw new FunctionException( message, args[" );
              sb.append( i );
              sb.append( "] );\n" );
              sb.append( "        }\n" );
              args.add( "new Percentage( new NumberValue( BigDecimal.valueOf( getPercentage( \"" + name + "\", " + i +  ", args ).getValue() ) ) )" );
            }
            else if( parameterType.equals( Dimension.class.getName() ) )
            {
              throw new IllegalStateException( "Not yet implemented - Dimension" );
            }
            else if( parameterType.equals( NumberValue.class.getName() ) )
            {
              sb.append( "        if( !isNumber( args[" );
              sb.append( i );
              sb.append( "] ) )\n" );
              sb.append( "        {\n" );
              sb.append( "          final String message = \"Argument " );
              sb.append( i + 1 );
              sb.append( " to function '"  );
              sb.append( name );
              sb.append( "' must be a number: %s\";\n" );
              sb.append( "          throw new FunctionException( message, args[" );
              sb.append( i );
              sb.append( "] );\n" );
              sb.append( "        }\n" );
              args.add( "new NumberValue( BigDecimal.valueOf( getNumber( \"" + name + "\", " + i +  ", args ).getValue() ) )" );
            }
            else if( "org.localmatters.lesscss4j.model.expression.ConstantColor".equals( parameterType ) )
            {
              sb.append( "        if( !isColor( args[" );
              sb.append( i );
              sb.append( "] ) )\n" );
              sb.append( "        {\n" );
              sb.append( "          final String message = \"Argument " );
              sb.append( i + 1 );
              sb.append( " to function '"  );
              sb.append( name );
              sb.append( "' must be a color: %s\";\n" );
              sb.append( "          throw new FunctionException( message, args[" );
              sb.append( i );
              sb.append( "] );\n" );
              sb.append( "        }\n" );
              args.add( "getColor( \"" + name + "\", " + i +  ", args )" );
            }
          }
          sb.append( "        return delegate." );
          sb.append( name );
          sb.append( "(" );
          for( int i = 0; i < args.size(); i++ )
          {
            if( 0 != i )
            {
              sb.append( ',' );
            }
            sb.append( args.get( i ) );
          }
          sb.append( ");\n" );
          sb.append( "      }\n" );
          sb.append( "    };\n" );
          sb.append( "    functions.put( \"" );
          sb.append( name );
          sb.append( "\", ");
          sb.append( name );
          sb.append( " );\n" );
        }
      }
    }
    sb.append( "    return functions;\n" );
    sb.append( "  }\n" );
    sb.append( "\n}\n" );

    final JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile( classname + CLASS_SUFFIX, type );
    try ( final Writer writer = sourceFile.openWriter() )
    {
      writer.write( sb.toString() );
    }
  }
}
