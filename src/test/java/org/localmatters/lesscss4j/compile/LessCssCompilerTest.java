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
import java.util.Comparator;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class LessCssCompilerTest
  extends AbstractLessCssCompilerTest
{
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
  public void Import()
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

  private void assertCompilesTo( final String key )
    throws IOException
  {
    compileAndCompare( toLessResourceName( key ), toCssResourceName( key ), null );
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

