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
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.localmatters.lesscss4j.error.WriterErrorHandler;
import org.localmatters.lesscss4j.output.PrettyPrintOptions;
import org.localmatters.lesscss4j.parser.UrlStyleSheetResource;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LessCssCompilerTest
{
  private static final String ENCODING = "UTF-8";

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
    final PrettyPrintOptions prettyPrintOptions = createPrettyPrintOptions();
    prettyPrintOptions.setSingleDeclarationOnOneLine( false );
    final String key = "css-big";
    compileAndCompare( toLessResourceName( key ), toCssResourceName( key ), prettyPrintOptions, null );
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
    compileAndCompare( "css/big.css", "css/big.css", createPrettyPrintOptions(), new Comparator<String>()
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
    final String error = ensureCompileError( "mixed-units-error", 3 );
    assertEquals( "mixed-units-error.less [1:4] - Unit mismatch: 1px 1%\n" +
                  "mixed-units-error.less [5:4] - Unit mismatch: #000 1em\n" +
                  "mixed-units-error.less [3:9] - Unit mismatch: 1px #fff\n",
                  error );
  }

  @Test
  public void undefinedVariable()
    throws IOException
  {
    final String error = ensureCompileError( "name-error-1.0", 2 );
    assertEquals( "name-error-1.0.less [1:5] - Undefined variable: @var\n" +
                  "name-error-1.0.less [3:10] - Undefined variable: @var2\n",
                  error );
  }

  @Test
  public void mixinErrors()
    throws IOException
  {
    final String error = ensureCompileError( "mixin-error", 3 );
    assertEquals( "mixin-error.less [2:2] - Undefined mixin: .mixin\n" +
                  "mixin-error.less [2:10] - Undefined mixin: .mixout\n" +
                  "mixin-error.less [11:2] - Mixin argument mismatch. Expected maximum of 2 but got 3.\n",
                  error );
  }

  @Test
  public void syntaxErrors()
    throws IOException
  {
    final String error = ensureCompileError( "syntax-error-1.0", 2 );
    assertEquals( "syntax-error-1.0.less [2:14] - no viable alternative at input ';'\n" +
                  "syntax-error-1.0.less [3:0] - missing EOF at '}'\n",
                  error );
  }

  @Test
  public void importMissingError()
    throws IOException
  {
    final String error = ensureCompileError( "import-error", 3 );

    final URL url = getClass().getClassLoader().getResource( "less/exceptions/import-error.less" );
    assertNotNull( url );
    final String baseDir = FilenameUtils.getFullPath( url.getPath() );

    assertEquals(
      "import-error.less [2:8] - Import error: \"bogus.less\": File '" + baseDir + "bogus.less' does not exist\n" +
      "imported-with-error.less [1:8] - Import error: url(nope.less): File '" +
      baseDir + "nope.less' does not exist\n" +
      "import-error.less [4:8] - Import error: 'missing.less': File '" + baseDir + "missing.less' does not exist\n",
      error );
  }

  @Test
  public void divideByZero()
    throws IOException
  {
    final String error = ensureCompileError( "divide-by-zero", 2 );
    assertEquals( "divide-by-zero.less [1:4] - Division by zero.\n" +
                  "divide-by-zero.less [2:4] - Division by zero.\n",
                  error );
  }

  private String ensureCompileError( final String lessFile, final int errorCount )
    throws IOException
  {
    final CompileResults compileResults = compile( "less/exceptions/" + lessFile + ".less" );
    assertEquals( errorCount, compileResults.getErrorCount() );
    return compileResults.getErrorOutput();
  }

  private void assertCompilesTo( final String key )
    throws IOException
  {
    compileAndCompare( toLessResourceName( key ), toCssResourceName( key ), createPrettyPrintOptions(), null );
  }

  private String readCss( final String cssFile )
    throws IOException
  {
    try ( final InputStream input = getClass().getClassLoader().getResourceAsStream( cssFile ) )
    {
      assertNotNull( input, "Unable to open " + cssFile );
      return IOUtils.toString( input, ENCODING );
    }
  }

  private PrettyPrintOptions createPrettyPrintOptions()
  {
    final PrettyPrintOptions prettyPrintOptions = new PrettyPrintOptions();
    prettyPrintOptions.setSingleDeclarationOnOneLine( true );
    prettyPrintOptions.setLineBetweenRuleSets( false );
    prettyPrintOptions.setOpeningBraceOnNewLine( false );
    prettyPrintOptions.setIndentSize( 2 );
    return prettyPrintOptions;
  }

  private void compileAndCompare( @Nonnull final String lessFile,
                                  @Nullable final String cssFile,
                                  @Nonnull final PrettyPrintOptions printOptions,
                                  @Nullable final Comparator<String> comparator )
    throws IOException
  {
    final CompileResults compileResults = compile( lessFile, printOptions );
    assertEquals( compileResults.getErrorCount(), 0 );
    if ( 0 == compileResults.getErrorCount() )
    {
      final String expected = readCss( cssFile );
      final String actual = compileResults.getOutput();
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

  private CompileResults compile( final String lessFile )
    throws IOException
  {
    return compile( lessFile, createPrettyPrintOptions() );
  }

  private CompileResults compile( final String lessFile, final PrettyPrintOptions printOptions )
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

      compiler.compile( new UrlStyleSheetResource( toUrl( lessFile ) ), output, errorHandler );
      output.close();
      return new CompileResults( output.toString( ENCODING ), writer.toString(), errorHandler.getErrorCount() );
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

