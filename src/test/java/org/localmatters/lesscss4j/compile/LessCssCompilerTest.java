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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Comparator;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.error.WriterErrorHandler;
import org.localmatters.lesscss4j.output.PrettyPrintOptions;
import org.localmatters.lesscss4j.parser.UrlStyleSheetResource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LessCssCompilerTest
{
  private static final String ENCODING = "UTF-8";

  private LessCssCompiler _compiler;
  private PrettyPrintOptions _printOptions;
  private ErrorHandler _errorHandler;
  private StringWriter _writer;

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

  @Test
  public void variables()
    throws IOException
  {
    assertCompilesTo( "variables" );
  }

  @Test
  public void lazyEvalVariables()
    throws IOException
  {
    assertCompilesTo( "lazy-eval" );
  }

  @Test
  public void plainCss()
    throws IOException
  {
    assertCompilesTo( "css" );
  }

  @Test
  public void comments()
    throws IOException
  {
    assertCompilesTo( "comments" );
  }

  @Test
  public void css3()
    throws IOException
  {
    assertCompilesTo( "css-3" );
  }

  @Test
  public void expressionParens()
    throws IOException
  {
    assertCompilesTo( "parens" );
  }

  @Test
  public void operations()
    throws IOException
  {
    assertCompilesTo( "operations" );
  }

  @Test
  public void strings()
    throws IOException
  {
    assertCompilesTo( "strings" );
  }

  @Test
  public void mixins()
    throws IOException
  {
    assertCompilesTo( "mixins" );
  }

  @Test
  public void nestedRuleSets()
    throws IOException
  {
    assertCompilesTo( "rulesets" );
  }

  @Test
  public void mixinVariableScope()
    throws IOException
  {
    assertCompilesTo( "scope" );
  }

  @Test
  public void mixinArgs()
    throws IOException
  {
    assertCompilesTo( "mixins-args" );
  }

  @Test
  public void multipleSelectors()
    throws IOException
  {
    assertCompilesTo( "selectors" );
  }

  @Test
  public void css3SingleRun()
    throws IOException
  {
    assertCompilesTo( "singlerun" );
  }

  @Test
  public void colorMath()
    throws IOException
  {
    assertCompilesTo( "colors" );
  }

  @Test
  public void atImport()
    throws IOException
  {
    assertCompilesTo( "import" );
  }

  @Test
  public void dashPrefix()
    throws IOException
  {
    assertCompilesTo( "dash-prefix" );
  }

  @Test
  public void internetExplorer()
    throws IOException
  {
    assertCompilesTo( "ie" );
  }

  @Test
  public void bigCssFile()
    throws IOException
  {
    _printOptions.setSingleDeclarationOnOneLine( false );
    assertCompilesTo( "css-big" );
  }

  @Test
  public void mediaAndPage()
    throws IOException
  {
    assertCompilesTo( "media-page" );
  }

  @Test
  public void accessors()
    throws IOException
  {
    assertCompilesTo( "accessors" );
  }

  @Test
  public void functions()
    throws IOException
  {
    assertCompilesTo( "functions" );
  }

  @Test
  public void keyframes()
    throws IOException
  {
    assertCompilesTo( "keyframes" );
  }

  @Test
  public void bigCssFileCompareToSelf()
    throws IOException
  {
    compileAndCompare( "css/big.css", "css/big.css", new Comparator<String>()
    {
      public int compare( final String expected, final String actual )
      {
        assertEquals( expected.toLowerCase(), actual.toLowerCase() );
        return 0;
      }
    } );
  }

  @Test
  public void mismatchedUnits()
    throws IOException
  {
    assertCompileError( "mixed-units-error" );
    assertEquals( "mixed-units-error.less [1:4] - Unit mismatch: 1px 1%\n" +
                  "mixed-units-error.less [5:4] - Unit mismatch: #000 1em\n" +
                  "mixed-units-error.less [3:9] - Unit mismatch: 1px #fff\n",
                  _writer.toString() );
    assertEquals( 3, _errorHandler.getErrorCount() );
  }

  @Test
  public void undefinedVariable()
    throws IOException
  {
    assertCompileError( "name-error-1.0" );
    assertEquals( "name-error-1.0.less [1:5] - Undefined variable: @var\n" +
                  "name-error-1.0.less [3:10] - Undefined variable: @var2\n",
                  _writer.toString() );
    assertEquals( 2, _errorHandler.getErrorCount() );
  }

  @Test
  public void mixinErrors()
    throws IOException
  {
    assertCompileError( "mixin-error" );
    assertEquals( "mixin-error.less [2:2] - Undefined mixin: .mixin\n" +
                  "mixin-error.less [2:10] - Undefined mixin: .mixout\n" +
                  "mixin-error.less [11:2] - Mixin argument mismatch. Expected maximum of 2 but got 3.\n",
                  _writer.toString() );
    assertEquals( 3, _errorHandler.getErrorCount() );
  }

  @Test
  public void syntaxErrors()
    throws IOException
  {
    assertCompileError( "syntax-error-1.0" );
    assertEquals( "syntax-error-1.0.less [2:14] - no viable alternative at input ';'\n" +
                  "syntax-error-1.0.less [3:0] - missing EOF at '}'\n",
                  _writer.toString() );
    assertEquals( 2, _errorHandler.getErrorCount() );
  }

  @Test
  public void importMissingError()
    throws IOException
  {
    assertCompileError( "import-error" );

    final URL url = getClass().getClassLoader().getResource( "less/exceptions/import-error.less" );
    assertNotNull( url );
    final String baseDir = FilenameUtils.getFullPath( url.getPath() );

    assertEquals(
      "import-error.less [2:8] - Import error: \"bogus.less\": File '" +
      baseDir +
      "bogus.less' does not exist\n" +
      "imported-with-error.less [1:8] - Import error: url(nope.less): File '" +
      baseDir +
      "nope.less' does not exist\n" +
      "import-error.less [4:8] - Import error: 'missing.less': File '" +
      baseDir +
      "missing.less' does not exist\n",
      _writer.toString() );
    assertEquals( 3, _errorHandler.getErrorCount() );
  }

  @Test
  public void divideByZero()
    throws IOException
  {
    assertCompileError( "divide-by-zero" );
    assertEquals( "divide-by-zero.less [1:4] - Division by zero.\n" +
                  "divide-by-zero.less [2:4] - Division by zero.\n",
                  _writer.toString() );
    assertEquals( 2, _errorHandler.getErrorCount() );
  }

  private void assertCompileError( final String lessFile )
    throws IOException
  {
    _writer = new StringWriter();

    _errorHandler = new WriterErrorHandler();
    ( (WriterErrorHandler) _errorHandler ).setLogStackTrace( false );
    ( (WriterErrorHandler) _errorHandler ).setWriter( new PrintWriter( _writer ) );

    compileAndCompare( "less/exceptions/" + lessFile + ".less", null, null );
  }

  private void assertCompilesTo( final String key )
    throws IOException
  {
    compileAndCompare( toLessResourceName( key ), toCssResourceName( key ), null );
  }

  private String readCss( final String cssFile )
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

  private void compileAndCompare( final String lessFile,
                                  final String cssFile,
                                  final Comparator<String> comparator )
    throws IOException
  {
    final URL url = toUrl( lessFile );

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

  private URL toUrl( final String resourceName )
  {
    final URL url = getClass().getClassLoader().getResource( resourceName );
    assertNotNull( url, "Unable to open " + resourceName );
    return url;
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

