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
package org.localmatters.lesscss4j.compile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Comparator;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.output.PrettyPrintOptions;
import org.localmatters.lesscss4j.parser.UrlStyleSheetResource;
import org.testng.annotations.BeforeMethod;
import static org.testng.Assert.*;

public abstract class AbstractLessCssCompilerTest
{
  public static final String ENCODING = "UTF-8";

  protected LessCssCompiler _compiler;
  protected PrettyPrintOptions _printOptions;
  protected ErrorHandler _errorHandler;

  @BeforeMethod
  public void setUp()
    throws Exception
  {
    _printOptions = new PrettyPrintOptions();
    _printOptions.setSingleDeclarationOnOneLine( true );
    _printOptions.setLineBetweenRuleSets( false );
    _printOptions.setOpeningBraceOnNewLine( false );
    _printOptions.setIndentSize( 2 );

    final DefaultLessCssCompilerFactory factoryBean = new DefaultLessCssCompilerFactory();
    factoryBean.setDefaultEncoding( ENCODING );
    factoryBean.setPrettyPrintEnabled( true );
    factoryBean.setPrettyPrintOptions( _printOptions );
    _compiler = factoryBean.create();
  }

  protected String readCss( final String cssFile )
    throws IOException
  {
    InputStream input = null;
    try
    {
      input = getClass().getClassLoader().getResourceAsStream( cssFile );
      assertNotNull( input, "Unable to open " + cssFile );
      return IOUtils.toString( input, ENCODING );
    }
    finally
    {
      IOUtils.closeQuietly( input );
    }
  }

  protected final void compileAndCompare( final String lessFile, final String cssFile, final Comparator<String> comparator )
    throws IOException
  {
    final URL url = getClass().getClassLoader().getResource( lessFile );
    assertNotNull( url, "Unable to open " + lessFile );

    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    _compiler.compile( new UrlStyleSheetResource( url ), output, _errorHandler );
    output.close();

    if ( null == _errorHandler || 0 == _errorHandler.getErrorCount() )
    {
      final String expected = readCss( cssFile );
      final String actual = output.toString( ENCODING );
      if ( null == comparator )
      {
        assertEquals( expected, actual );
      }
      else
      {
        comparator.compare( expected, actual );
      }
    }
  }

  private String toCssResourceName( final String key )
  {
    return "css/" + key + ".css";
  }

  private String toLessResourceName( final String key )
  {
    return "less/" + key + ".less";
  }
}
