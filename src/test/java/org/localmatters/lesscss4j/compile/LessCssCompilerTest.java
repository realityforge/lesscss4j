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
  public void Variables()
    throws IOException
  {
    compileAndValidate( "less/variables.less", "css/variables.css" );
  }

  @Test
  public void LazyEvalVariables()
    throws IOException
  {
    compileAndValidate( "less/lazy-eval.less", "css/lazy-eval.css" );
  }

  @Test
  public void PlainCss()
    throws IOException
  {
    compileAndValidate( "less/css.less", "css/css.css" );
  }

  @Test
  public void Comments()
    throws IOException
  {
    compileAndValidate( "less/comments.less", "css/comments.css" );
  }

  @Test
  public void Css3()
    throws IOException
  {
    compileAndValidate( "less/css-3.less", "css/css-3.css" );
  }

  @Test
  public void ExpressionParens()
    throws IOException
  {
    compileAndValidate( "less/parens.less", "css/parens.css" );
  }

  @Test
  public void Operations()
    throws IOException
  {
    compileAndValidate( "less/operations.less", "css/operations.css" );
  }

  @Test
  public void Strings()
    throws IOException
  {
    compileAndValidate( "less/strings.less", "css/strings.css" );
  }

  @Test
  public void Mixins()
    throws IOException
  {
    compileAndValidate( "less/mixins.less", "css/mixins.css" );
  }

  @Test
  public void NestedRuleSets()
    throws IOException
  {
    compileAndValidate( "less/rulesets.less", "css/rulesets.css" );
  }

  @Test
  public void MixinVariableScope()
    throws IOException
  {
    compileAndValidate( "less/scope.less", "css/scope.css" );
  }

  @Test
  public void MixinArgs()
    throws IOException
  {
    compileAndValidate( "less/mixins-args.less", "css/mixins-args.css" );
  }

  @Test
  public void MultipleSelectors()
    throws IOException
  {
    compileAndValidate( "less/selectors.less", "css/selectors.css" );
  }

  @Test
  public void Css3SingleRun()
    throws IOException
  {
    compileAndValidate( "less/singlerun.less", "css/singlerun.css" );
  }

  @Test
  public void ColorMath()
    throws IOException
  {
    compileAndValidate( "less/colors.less", "css/colors.css" );
  }

  @Test
  public void Import()
    throws IOException
  {
    compileAndValidate( "less/import.less", "css/import.css" );
  }

  @Test
  public void DashPrefix()
    throws IOException
  {
    compileAndValidate( "less/dash-prefix.less", "css/dash-prefix.css" );
  }

  @Test
  public void InternetExplorer()
    throws IOException
  {
    compileAndValidate( "less/ie.less", "css/ie.css" );
  }

  @Test
  public void BigCssFile()
    throws IOException
  {
    _printOptions.setSingleDeclarationOnOneLine( false );
    compileAndValidate( "less/css-big.less", "css/css-big.css" );
  }

  @Test
  public void MediaAndPage()
    throws IOException
  {
    compileAndValidate( "less/media-page.less", "css/media-page.css" );
  }

  @Test
  public void Accessors()
    throws IOException
  {
    compileAndValidate( "less/accessors.less", "css/accessors.css" );
  }

  @Test
  public void Functions()
    throws IOException
  {
    compileAndValidate( "less/functions.less", "css/functions.css" );
  }

  @Test
  public void Keyframes()
    throws IOException
  {
    compileAndValidate( "less/keyframes.less", "css/keyframes.css" );
  }

  @Test
  public void BigCssFileCompareToSelf()
    throws IOException
  {
    compileAndValidate( "css/big.css", "css/big.css", new Comparator<String>()
    {
      public int compare( final String expected, final String actual )
      {
        assertEquals( expected.toLowerCase(), actual.toLowerCase() );
        return 0;
      }
    } );
  }
}

