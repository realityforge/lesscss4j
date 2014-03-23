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
package org.localmatters.lesscss4j.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.error.ErrorUtils;
import org.localmatters.lesscss4j.error.LessCssException;
import org.localmatters.lesscss4j.factory.ObjectFactory;
import org.localmatters.lesscss4j.factory.StyleSheetFactory;
import org.localmatters.lesscss4j.model.StyleSheet;
import org.localmatters.lesscss4j.parser.antlr.LessCssLexer;
import org.localmatters.lesscss4j.parser.antlr.LessCssParser;

public class LessCssStyleSheetParser
  implements StyleSheetParser, StyleSheetTreeParser
{
  private String _defaultEncoding = "UTF-8";
  private int _initialBufferSize = ANTLRInputStream.INITIAL_BUFFER_SIZE;
  private int _readBufferSize = ANTLRInputStream.READ_BUFFER_SIZE;
  private ObjectFactory<StyleSheet> _styleSheetFactory;

  public static final String CHARSET_SYM = "@charset";
  public static final String NEWLINE_CHARS = "\n\r\f";

  public String getDefaultEncoding()
  {
    return _defaultEncoding;
  }

  public void setDefaultEncoding( final String defaultEncoding )
  {
    _defaultEncoding = defaultEncoding;
  }

  public int getInitialBufferSize()
  {
    return _initialBufferSize;
  }

  public void setInitialBufferSize( final int initialBufferSize )
  {
    _initialBufferSize = initialBufferSize;
  }

  public int getReadBufferSize()
  {
    return _readBufferSize;
  }

  public void setReadBufferSize( final int readBufferSize )
  {
    _readBufferSize = readBufferSize;
  }

  public ObjectFactory<StyleSheet> getStyleSheetFactory()
  {
    if ( null == _styleSheetFactory )
    {
      _styleSheetFactory = createDefaultStyleSheetFactory();
    }
    return _styleSheetFactory;
  }

  public void setStyleSheetFactory( final ObjectFactory<StyleSheet> styleSheetFactory )
  {
    _styleSheetFactory = styleSheetFactory;
  }

  protected ObjectFactory<StyleSheet> createDefaultStyleSheetFactory()
  {
    final StyleSheetFactory styleSheetObjectFactory =
      (StyleSheetFactory) StyleSheetFactory.createDefaultObjectFactory();
    styleSheetObjectFactory.setStyleSheetTreeParser( this );
    return styleSheetObjectFactory;
  }

  @Nullable
  public StyleSheet parse( @Nonnull final StyleSheetResource input, @Nullable final ErrorHandler errorHandler )
    throws IOException
  {
    final Tree parseTree = parseTree( input, errorHandler );
    if ( null != parseTree )
    {
      return getStyleSheetFactory().create( new StyleSheetTree( parseTree, input ), errorHandler );
    }
    else
    {
      return null;
    }
  }

  public Tree parseTree( @Nonnull final StyleSheetResource input, @Nullable final ErrorHandler errorHandler )
    throws IOException
  {
    final LessCssLexer lexer = new LessCssLexer( createANTLRInputStream( input.getInputStream() ) );
    final LessCssParser parser = new LessCssParser( new CommonTokenStream( lexer ) );
    try
    {
      parser.setErrorHandler( errorHandler );
      final LessCssParser.styleSheet_return result = parser.styleSheet();
      Tree parseTree = null;
      if ( parser.getErrorCount() == 0 )
      {
        parseTree = (Tree) result.getTree();
      }
      return parseTree;
    }
    catch ( final RecognitionException e )
    {
      ErrorUtils.handleError( errorHandler, e, parser );
    }
    catch ( final LessCssException e )
    {
      ErrorUtils.handleError( errorHandler, e );
    }
    return null;
  }

  protected ANTLRInputStream createANTLRInputStream( final InputStream input )
    throws IOException
  {
    String encoding = null;

    // Read a buffer of data to see if we can find the @charset symbol in the beginning of the file.
    // Otherwise just use the default encoding.
    try ( final PushbackInputStream pushbackStream = new PushbackInputStream( input, getReadBufferSize() ) )
    {
      final byte[] buf = new byte[ getReadBufferSize() ];
      final int len = pushbackStream.read( buf, 0, buf.length );
      if ( len >= 0 )
      {
        pushbackStream.unread( buf, 0, len );
        final String bufStr = new String( buf, 0, len, "ASCII" );
        encoding = parseCharset( bufStr );
      }

      if ( null == encoding )
      {
        encoding = getDefaultEncoding();
      }

      return new ANTLRInputStream( pushbackStream, getInitialBufferSize(), getReadBufferSize(), encoding );
    }
  }

  /**
   * Attempt to find a @charset directive in the given string buffer.
   *
   * @param buffer The buffer to parse
   * @return The parsed charset name
   * @throws IOException
   */
  protected String parseCharset( final String buffer )
    throws IOException
  {
    int idx = 0;
    boolean comment = false;
    while ( idx < buffer.length() )
    {
      if ( comment )
      {
        // block comment contents
        if ( buffer.regionMatches( idx, "*/", 0, 2 ) )
        {
          idx += 2;
          comment = false;
        }
        else
        {
          idx++;
        }
      }
      else if ( buffer.regionMatches( idx, "//", 0, 2 ) )
      {
        // line comment
        idx += 2;
        while ( idx < buffer.length() && NEWLINE_CHARS.indexOf( buffer.charAt( idx ) ) < 0 )
        {
          idx++;
        }
      }
      else if ( buffer.regionMatches( idx, "/*", 0, 2 ) )
      {
        // Start of block comment
        idx += 2;
        comment = true;
      }
      else if ( Character.isWhitespace( buffer.charAt( idx ) ) )
      {
        idx++;
      }
      else if ( buffer.regionMatches( idx, CHARSET_SYM, 0, CHARSET_SYM.length() ) )
      {
        // Charset symbol
        idx += CHARSET_SYM.length();
        while ( Character.isWhitespace( buffer.charAt( idx ) ) )
        {
          idx++;
        }

        // We should be at either a single quote or double quote
        final char quoteChar = buffer.charAt( idx++ );
        if ( quoteChar != '\'' && quoteChar != '"' )
        {
          throw new IOException( "Invalid " + CHARSET_SYM + " specification" );
        }

        // Find the closing quote
        final int startIdx = idx;
        while ( idx < buffer.length() &&
                NEWLINE_CHARS.indexOf( buffer.charAt( idx ) ) < 0 &&
                buffer.charAt( idx ) != quoteChar )
        {
          idx++;
        }

        if ( idx >= buffer.length() )
        {
          throw new IOException( "Unbalanced quote in " + CHARSET_SYM );
        }

        return buffer.substring( startIdx, idx );
      }
      else
      {
        // non whitespace.  @charset must be first thing in the file, so we can stop looking for one now.
        return null;
      }
    }
    return null;
  }
}
