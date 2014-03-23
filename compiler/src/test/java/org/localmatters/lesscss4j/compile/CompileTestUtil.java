package org.localmatters.lesscss4j.compile;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.localmatters.lesscss4j.error.WriterErrorHandler;
import org.localmatters.lesscss4j.output.PrettyPrintOptions;
import org.localmatters.lesscss4j.parser.UrlStyleSheetResource;
import static org.testng.Assert.*;

/**
 * Utility class for testing compilation and comparison of less/css files.
 */
final class CompileTestUtil
{
  private static final String ENCODING = "UTF-8";

  private CompileTestUtil()
  {
  }

  static String readResourceFully( final String cssFile )
    throws IOException
  {
    try ( final InputStream input = getResourceAsStream( cssFile ) )
    {
      return IOUtils.toString( input, CompileTestUtil.ENCODING );
    }
  }

  static CompileResults compile( final String lessFile, final PrettyPrintOptions printOptions )
    throws IOException
  {
    final DefaultLessCssCompilerFactory factoryBean = new DefaultLessCssCompilerFactory();
    factoryBean.setDefaultEncoding( ENCODING );
    factoryBean.setPrettyPrintEnabled( true );
    factoryBean.setPrettyPrintOptions( printOptions );
    final LessCssCompiler compiler = factoryBean.create();

    try ( final StringWriter writer = new StringWriter(); final ByteArrayOutputStream output = new ByteArrayOutputStream() )
    {
      final WriterErrorHandler errorHandler = new WriterErrorHandler();
      errorHandler.setLogStackTrace( false );
      errorHandler.setWriter( new PrintWriter( writer ) );

      compiler.compile( new UrlStyleSheetResource( getResource( lessFile ) ), output, errorHandler );
      output.close();
      return new CompileResults( output.toString( ENCODING ), writer.toString(), errorHandler.getErrorCount() );
    }
  }

  private static URL getResource( final String resourceName )
  {
    final URL url = CompileTestUtil.class.getClassLoader().getResource( resourceName );
    assertNotNull( url, "Unable to open " + resourceName );
    return url;
  }

  private static InputStream getResourceAsStream( final String resourceName )
  {
    final InputStream inputStream = CompileTestUtil.class.getClassLoader().getResourceAsStream( resourceName );
    assertNotNull( inputStream, "Unable to open " + resourceName );
    return inputStream;
  }
}
