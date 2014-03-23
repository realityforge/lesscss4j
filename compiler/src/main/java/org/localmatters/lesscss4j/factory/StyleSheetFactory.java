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
package org.localmatters.lesscss4j.factory;

import java.io.IOException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.io.FilenameUtils;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.error.ErrorUtils;
import org.localmatters.lesscss4j.error.ImportException;
import org.localmatters.lesscss4j.model.AbstractElement;
import org.localmatters.lesscss4j.model.Keyframes;
import org.localmatters.lesscss4j.model.Media;
import org.localmatters.lesscss4j.model.Page;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.StyleSheet;
import org.localmatters.lesscss4j.model.expression.Expression;
import org.localmatters.lesscss4j.parser.DefaultStyleSheetResourceLoader;
import org.localmatters.lesscss4j.parser.StyleSheetResource;
import org.localmatters.lesscss4j.parser.StyleSheetResourceLoader;
import org.localmatters.lesscss4j.parser.StyleSheetTree;
import org.localmatters.lesscss4j.parser.StyleSheetTreeParser;
import static org.localmatters.lesscss4j.parser.antlr.LessCssLexer.*;

public class StyleSheetFactory
  extends AbstractObjectFactory<StyleSheet>
{
  private ObjectFactory<RuleSet> _ruleSetFactory;
  private ObjectFactory<Media> _mediaFactory;
  private ObjectFactory<Keyframes> _keyframesFactory;
  private ObjectFactory<Page> _pageFactory;
  private ObjectFactory<Expression> _expressionFactory;
  private StyleSheetTreeParser _styleSheetTreeParser;
  private StyleSheetResourceLoader _styleSheetResourceLoader = new DefaultStyleSheetResourceLoader();

  /**
   * Pattern to extract the path from an <code>@import</code> statement.
   * Possible options (whitespace is insignificant):
   * <ul>
   * <li>url("some/path")</li>
   * <li>url('some/path')</li>
   * <li>url(some/path)</li>
   * <li>"some/path"</li>
   * <li>'some/path'</li>
   * </ul>
   */
  private final Pattern _importCleanupPattern =
    Pattern.compile( "(?i:u\\s*r\\s*l\\(\\s*['\"]?|['\"])(.*?)(?:['\"]?\\s*\\)|['\"])" );

  public StyleSheetTreeParser getStyleSheetTreeParser()
  {
    return _styleSheetTreeParser;
  }

  public void setStyleSheetTreeParser( final StyleSheetTreeParser styleSheetTreeParser )
  {
    _styleSheetTreeParser = styleSheetTreeParser;
  }

  public ObjectFactory<Expression> getExpressionFactory()
  {
    return _expressionFactory;
  }

  public void setExpressionFactory( final ObjectFactory<Expression> expressionFactory )
  {
    _expressionFactory = expressionFactory;
  }

  public ObjectFactory<RuleSet> getRuleSetFactory()
  {
    return _ruleSetFactory;
  }

  public void setRuleSetFactory( final ObjectFactory<RuleSet> ruleSetFactory )
  {
    _ruleSetFactory = ruleSetFactory;
  }

  public ObjectFactory<Media> getMediaFactory()
  {
    return _mediaFactory;
  }

  public void setMediaFactory( final ObjectFactory<Media> mediaFactory )
  {
    _mediaFactory = mediaFactory;
  }

  public ObjectFactory<Keyframes> getKeyframesFactory()
  {
    return _keyframesFactory;
  }

  public void setKeyframesFactory( final ObjectFactory<Keyframes> keyframesFactory )
  {
    _keyframesFactory = keyframesFactory;
  }

  public ObjectFactory<Page> getPageFactory()
  {
    return _pageFactory;
  }

  public void setPageFactory( final ObjectFactory<Page> pageFactory )
  {
    _pageFactory = pageFactory;
  }

  @Nullable
  public StyleSheet create( @Nonnull final Tree styleSheetNode, @Nullable final ErrorHandler errorHandler )
  {
    final StyleSheetResource resource =
      styleSheetNode instanceof StyleSheetTree ? ( (StyleSheetTree) styleSheetNode ).getResource() : null;

    final StyleSheet stylesheet = new StyleSheet();
    stylesheet.setLine( styleSheetNode.getLine() );
    stylesheet.setChar( styleSheetNode.getCharPositionInLine() );

    processStyleSheet( stylesheet, styleSheetNode, resource, errorHandler );

    return stylesheet;
  }

  protected void processStyleSheet( final StyleSheet stylesheet,
                                    final Tree styleSheetNode,
                                    final StyleSheetResource resource,
                                    final ErrorHandler errorHandler )
  {
    for ( int idx = 0, numChildren = styleSheetNode.getChildCount(); idx < numChildren; idx++ )
    {
      final Tree child = styleSheetNode.getChild( idx );
      processStyleSheetNode( stylesheet, child, resource, errorHandler );
    }
  }

  protected void processStyleSheetNode( final StyleSheet stylesheet,
                                        final Tree child,
                                        final StyleSheetResource resource,
                                        final ErrorHandler errorHandler )
  {
    switch ( child.getType() )
    {
      case CHARSET:
        String charset = child.getChild( 0 ).getText();
        charset = charset.replaceFirst( "['\"]\\s*(\\S*)\\s*['\"]", "$1" );
        if ( charset.length() > 0 )
        {
          stylesheet.setCharset( charset );
        }
        break;

      case IMPORT:
        handleImport( stylesheet, child, resource, errorHandler );
        break;

      case VAR:
        final Tree exprNode = child.getChild( 1 );
        final Expression expr = getExpressionFactory().create( exprNode, errorHandler );
        if ( null != expr )
        {
          stylesheet.setVariable( child.getChild( 0 ).getText(), expr );
        }
        break;

      case MIXIN_MACRO:
      case RULESET:
        final RuleSet ruleSet = getRuleSetFactory().create( child, errorHandler );
        if ( null != ruleSet )
        {
          stylesheet.addBodyElement( ruleSet );
        }
        break;

      case MEDIA_SYM:
        final Media media = getMediaFactory().create( child, errorHandler );
        if ( null != media )
        {
          stylesheet.addBodyElement( media );
        }
        break;

      case KEYFRAMES:
        final Keyframes keyframes = getKeyframesFactory().create( child, errorHandler );
        if ( null != keyframes )
        {
          stylesheet.addBodyElement( keyframes );
        }
        break;

      case PAGE_SYM:
        final Page page = getPageFactory().create( child, errorHandler );
        if ( null != page )
        {
          stylesheet.addBodyElement( page );
        }
        break;

      default:
        handleUnexpectedChild( "Unexpected stylesheet child:", child );
        break;
    }
  }

  protected void handleImport( final StyleSheet stylesheet,
                               final Tree importNode,
                               final StyleSheetResource resource,
                               final ErrorHandler errorHandler )
  {
    final String importUrl = importNode.getChild( 0 ).getText();
    try
    {
      final String path = cleanImportPath( importUrl );

      // circular/duplicate import check
      if ( !stylesheet.getImports().contains( path ) )
      {
        stylesheet.addImport( path );
        importStylesheet( path, resource, stylesheet, errorHandler );
      }
    }
    catch ( final IOException e )
    {
      ErrorUtils.handleError( errorHandler,
                              new AbstractElement( importNode.getLine(), importNode.getCharPositionInLine() ),
                              new ImportException( e.getMessage(), importUrl, e ) );
    }
  }

  protected void importStylesheet( final String path,
                                   final StyleSheetResource relativeTo,
                                   final StyleSheet stylesheet,
                                   final ErrorHandler errorHandler )
    throws IOException
  {
    Object saveContext = null;
    if ( null != errorHandler )
    {
      saveContext = errorHandler.getContext();
      errorHandler.setContext( path );
    }
    try
    {
      final StyleSheetResource importResource = getImportResource( path, relativeTo );
      final int preImportErrorCount = null != errorHandler ? errorHandler.getErrorCount() : 0;
      final Tree result = getStyleSheetTreeParser().parseTree( importResource, errorHandler );
      if ( null == errorHandler || preImportErrorCount == errorHandler.getErrorCount() )
      {
        processStyleSheet( stylesheet, result, importResource, errorHandler );
      }
    }
    finally
    {
      if ( null != errorHandler )
      {
        errorHandler.setContext( saveContext );
      }
    }
  }

  protected StyleSheetResource getImportResource( String path, final StyleSheetResource relativeTo )
    throws IOException
  {
    final URL importUrl;
    if ( isAbsoluteUrl( path ) )
    {
      importUrl = new URL( path );
    }
    else
    {
      final String extension = FilenameUtils.getExtension( path );
      if ( null == extension || ( !"css".equals( extension ) && !"less".equals( extension ) ) )
      {
        path = path + ".less";
      }
      importUrl = new URL( relativeTo.getUrl(), path );
    }

    return getStyleSheetResourceLoader().getResource( importUrl );
  }

  protected String cleanImportPath( final String path )
  {
    final Matcher matcher = _importCleanupPattern.matcher( path );
    if ( matcher.matches() )
    {
      return matcher.group( 1 );
    }
    else
    {
      throw new ImportException( "Unsupported import path: ", path, null );
    }
  }

  public StyleSheetResourceLoader getStyleSheetResourceLoader()
  {
    return _styleSheetResourceLoader;
  }

  public void setStyleSheetResourceLoader( final StyleSheetResourceLoader styleSheetResourceLoader )
  {
    _styleSheetResourceLoader = styleSheetResourceLoader;
  }

  /**
   * Valid characters in a scheme.
   * <p/>
   * RFC 1738 says the following:
   * <p/>
   * <blockquote> Scheme names consist of a sequence of characters. The lower case letters "a"--"z",
   * digits, and the characters plus ("+"), period ("."), and hyphen ("-") are allowed. For resiliency,
   * programs interpreting URLs should treat upper case letters as equivalent to lower case in scheme
   * names (e.g., allow "HTTP" as well as "http"). </blockquote>
   * <p/>
   * We treat as absolute any URL that begins with such a scheme name, followed by a colon.
   */
  public static final String VALID_SCHEME_CHARS =
    "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789+.-";

  /**
   * Determines if the given URL is absolute or not.  An absolute url starts with a scheme and colon.
   * (i.e. http:, ftp:, etc.)
   * <p/>
   * This is copied from apache taglibs ImportSupport class
   *
   * @param url The url to check
   * @return True if the url is an absolute url.
   */
  public static boolean isAbsoluteUrl( final String url )
  {
    // a null URL is not absolute, by our definition
    if ( null == url )
    {
      return false;
    }

    // do a fast, simple check first
    final int colonPos;
    if ( ( colonPos = url.indexOf( ":" ) ) == -1 )
    {
      return false;
    }

    // if we DO have a colon, make sure that every character
    // leading up to it is a valid scheme character
    for ( int i = 0; i < colonPos; i++ )
    {
      if ( VALID_SCHEME_CHARS.indexOf( url.charAt( i ) ) == -1 )
      {
        return false;
      }
    }

    // if so, we've got an absolute url
    return true;
  }

  public static ObjectFactory<StyleSheet> createDefaultObjectFactory()
  {
    final ExpressionFactory expressionFactory = new ExpressionFactory();
    final SelectorFactory selectorFactory = new SelectorFactory();

    final DeclarationFactory declarationFactory = new DeclarationFactory();
    declarationFactory.setExpressionFactory( expressionFactory );
    declarationFactory.setSelectorFactory( selectorFactory );

    final RuleSetFactory ruleSetFactory = new RuleSetFactory();
    ruleSetFactory.setSelectorFactory( selectorFactory );
    ruleSetFactory.setDeclarationFactory( declarationFactory );
    ruleSetFactory.setExpressionFactory( expressionFactory );

    final MediaFactory mediaFactory = new MediaFactory();
    mediaFactory.setRuleSetFactory( ruleSetFactory );
    mediaFactory.setExpressionFactory( expressionFactory );

    final KeyframesFactory keyframesFactory = new KeyframesFactory();
    keyframesFactory.setRuleSetFactory( ruleSetFactory );
    keyframesFactory.setExpressionFactory( expressionFactory );

    final PageFactory pageFactory = new PageFactory();
    pageFactory.setDeclarationFactory( declarationFactory );
    pageFactory.setExpressionFactory( expressionFactory );

    final StyleSheetFactory styleSheetFactory = new StyleSheetFactory();
    styleSheetFactory.setRuleSetFactory( ruleSetFactory );
    styleSheetFactory.setMediaFactory( mediaFactory );
    styleSheetFactory.setPageFactory( pageFactory );
    styleSheetFactory.setExpressionFactory( expressionFactory );
    styleSheetFactory.setKeyframesFactory( keyframesFactory );

    return styleSheetFactory;
  }
}

