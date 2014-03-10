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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;
import org.localmatters.lesscss4j.error.WriterErrorHandler;

public class LessCssCompilerErrorTest
  extends AbstractLessCssCompilerTest
{
  StringWriter _writer;

  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();

    _writer = new StringWriter();

    _errorHandler = new WriterErrorHandler();
    ( (WriterErrorHandler) _errorHandler ).setLogStackTrace( false );
    ( (WriterErrorHandler) _errorHandler ).setWriter( new PrintWriter( _writer ) );
  }

  public void testMismatchedUnits()
    throws IOException
  {
    compileAndValidate( "less/exceptions/mixed-units-error.less", null );
    assertEquals( "mixed-units-error.less [1:4] - Unit mismatch: 1px 1%\n" +
                  "mixed-units-error.less [5:4] - Unit mismatch: #000 1em\n" +
                  "mixed-units-error.less [3:9] - Unit mismatch: 1px #fff\n",
                  _writer.toString() );
    assertEquals( 3, _errorHandler.getErrorCount() );
  }

  public void testUndefinedVariable()
    throws IOException
  {
    compileAndValidate( "less/exceptions/name-error-1.0.less", null );
    assertEquals( "name-error-1.0.less [1:5] - Undefined variable: @var\n" +
                  "name-error-1.0.less [3:10] - Undefined variable: @var2\n",
                  _writer.toString() );
    assertEquals( 2, _errorHandler.getErrorCount() );
  }

  public void testMixinErrors()
    throws IOException
  {
    compileAndValidate( "less/exceptions/mixin-error.less", null );
    assertEquals( "mixin-error.less [2:2] - Undefined mixin: .mixin\n" +
                  "mixin-error.less [2:10] - Undefined mixin: .mixout\n" +
                  "mixin-error.less [11:2] - Mixin argument mismatch. Expected maximum of 2 but got 3.\n",
                  _writer.toString() );
    assertEquals( 3, _errorHandler.getErrorCount() );
  }

  public void testSyntaxErrors()
    throws IOException
  {
    compileAndValidate( "less/exceptions/syntax-error-1.0.less", null );
    assertEquals( "syntax-error-1.0.less [2:14] - no viable alternative at input ';'\n" +
                  "syntax-error-1.0.less [3:0] - missing EOF at '}'\n",
                  _writer.toString() );
    assertEquals( 2, _errorHandler.getErrorCount() );
  }

  public void testImportMissingError()
    throws IOException
  {
    final String resource = "less/exceptions/import-error.less";
    compileAndValidate( resource, null );

    final URL url = getClass().getClassLoader().getResource( resource );
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

  public void testDivideByZero()
    throws IOException
  {
    compileAndValidate( "less/exceptions/divide-by-zero.less", null );
    assertEquals( "divide-by-zero.less [1:4] - Division by zero.\n" +
                  "divide-by-zero.less [2:4] - Division by zero.\n",
                  _writer.toString() );
    assertEquals( 2, _errorHandler.getErrorCount() );
  }
}

