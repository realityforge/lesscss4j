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
package org.localmatters.lesscss4j.output;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import org.localmatters.lesscss4j.error.ErrorHandler;
import org.localmatters.lesscss4j.model.BodyElement;
import org.localmatters.lesscss4j.model.Declaration;
import org.localmatters.lesscss4j.model.DeclarationElement;
import org.localmatters.lesscss4j.model.Keyframes;
import org.localmatters.lesscss4j.model.Media;
import org.localmatters.lesscss4j.model.Page;
import org.localmatters.lesscss4j.model.RuleSet;
import org.localmatters.lesscss4j.model.Selector;
import org.localmatters.lesscss4j.model.StyleSheet;

// todo: It might make sense to break this up into separate writers for each type of element in the stylesheet
public class StyleSheetWriterImpl
  implements StyleSheetWriter
{
  private boolean _prettyPrintEnabled = false;
  private String _defaultEncoding = "UTF-8";
  private String _newline = "\n";
  private PrettyPrintOptions _prettyPrintOptions;

  public PrettyPrintOptions getPrettyPrintOptions()
  {
    if ( null == _prettyPrintOptions )
    {
      _prettyPrintOptions = new PrettyPrintOptions();
    }
    return _prettyPrintOptions;
  }

  public void setPrettyPrintOptions( final PrettyPrintOptions prettyPrintOptions )
  {
    _prettyPrintOptions = prettyPrintOptions;
  }

  public String getNewline()
  {
    return _newline;
  }

  public void setNewline( final String newline )
  {
    _newline = newline;
  }

  public String getDefaultEncoding()
  {
    return _defaultEncoding;
  }

  public void setDefaultEncoding( final String defaultEncoding )
  {
    _defaultEncoding = defaultEncoding;
  }

  public boolean isPrettyPrintEnabled()
  {
    return _prettyPrintEnabled;
  }

  public void setPrettyPrintEnabled( final boolean prettyPrintEnabled )
  {
    _prettyPrintEnabled = prettyPrintEnabled;
  }

  public void write( final OutputStream output, final StyleSheet styleSheet, final ErrorHandler errorHandler )
    throws IOException
  {
    final String encoding = getEncoding( styleSheet );
    try ( final Writer writer = new BufferedWriter( new OutputStreamWriter( output, encoding ) ) )
    {
      write( writer, styleSheet );
    }
  }

  private String getEncoding( final StyleSheet styleSheet )
  {
    final String encoding = styleSheet.getCharset();
    if ( null == encoding || 0 == encoding.length() )
    {
      return getDefaultEncoding();
    }
    else
    {
      return encoding;
    }
  }

  private void writeBreak( final Writer writer, final int indent )
    throws IOException
  {
    if ( isPrettyPrintEnabled() )
    {
      writer.write( getNewline() );
      if ( indent > 0 )
      {
        writeIndent( writer, indent );
      }
    }
  }

  private void writeBreak( final Writer writer )
    throws IOException
  {
    writeBreak( writer, 0 );
  }

  private void writeSpace( final Writer writer )
    throws IOException
  {
    if ( isPrettyPrintEnabled() )
    {
      writer.write( " " );
    }
  }

  private void writeSemi( final Writer writer, final boolean withBreak )
    throws IOException
  {
    writer.write( ";" );
    if ( withBreak )
    {
      writeBreak( writer );
    }
  }

  private void writeSemi( final Writer writer )
    throws IOException
  {
    writeSemi( writer, true );
  }

  private void writeSeparator( final Writer writer, final String separator )
    throws IOException
  {
    writer.write( separator );
    writeSpace( writer );
  }

  private void writeIndent( final Writer writer, final int level )
    throws IOException
  {
    if ( isPrettyPrintEnabled() )
    {
      for ( int idx = 0; idx < level; idx++ )
      {
        for ( int jdx = 0; jdx < getPrettyPrintOptions().getIndentSize(); jdx++ )
        {
          writer.write( ' ' );
        }
      }
    }
  }

  private void write( final Writer writer, final StyleSheet styleSheet )
    throws IOException
  {
    writeCharset( writer, styleSheet );
    writeImports( writer, styleSheet );

    writeBodyElements( writer, styleSheet.getBodyElements(), 0 );
  }

  private void writeImports( final Writer writer, final StyleSheet styleSheet )
    throws IOException
  {
    for ( final String importElement : styleSheet.getImports() )
    {
      writer.write( "@import " );
      if ( "'\"".indexOf( importElement.charAt( 0 ) ) < 0 && !importElement.startsWith( "url" ) )
      {
        writer.write( "'" );
        writer.write( importElement );
        writer.write( "'" );
      }
      else
      {
        writer.write( importElement );
      }
      writeSemi( writer );
    }
  }

  private void writeCharset( final Writer writer, final StyleSheet styleSheet )
    throws IOException
  {
    if ( null != styleSheet.getCharset() && 0 < styleSheet.getCharset().length() )
    {
      writer.write( "@charset '" );
      writer.write( styleSheet.getCharset() );
      writeSemi( writer );
    }
  }

  private void writeBodyElements( final Writer writer,
                                  final List<BodyElement> bodyElements,
                                  final int indent )
    throws IOException
  {
    if ( null == bodyElements )
    {
      return;
    }

    for ( int i = 0, bodyElementsSize = bodyElements.size(); i < bodyElementsSize; i++ )
    {
      final BodyElement element = bodyElements.get( i );
      if ( i > 0 && isPrettyPrintEnabled() && getPrettyPrintOptions().isLineBetweenRuleSets() )
      {
        writeBreak( writer );
      }
      writeIndent( writer, indent );
      if ( element instanceof Media )
      {
        writeMedia( writer, (Media) element, indent );
      }
      else if ( element instanceof Page )
      {
        writePage( writer, (Page) element, indent );
      }
      else if ( element instanceof Keyframes )
      {
        writeKeyframes( writer, (Keyframes) element, indent );
      }
      else if ( element instanceof RuleSet )
      {
        writeRuleSet( writer, (RuleSet) element, indent );
      }
    }
  }

  private void writePage( final Writer writer, final Page page, final int indent )
    throws IOException
  {
    final List<DeclarationElement> declarations = page.getDeclarations();
    if ( null == declarations || 0 == declarations.size() )
    {
      return;
    }

    writer.write( "@page" );

    if ( null != page.getPseudoPage() )
    {
      writer.write( " :" );
      writer.write( page.getPseudoPage() );
    }

    writeOpeningBrace( writer, indent, declarations );
    writeBreak( writer, indent );

    writeDeclarations( writer, declarations, indent );

    writeDeclarationBraceSpace( writer, declarations );

    if ( !isOneLineDeclarationList( declarations ) )
    {
      writeIndent( writer, indent );
    }
    writeClosingBrace( writer, indent );
  }

  private void writeMedia( final Writer writer, final Media media, final int indent )
    throws IOException
  {
    writer.write( "@media " );

    boolean first = true;
    for ( final String medium : media.getMediums() )
    {
      if ( !first )
      {
        writeSeparator( writer, "," );
      }
      writer.write( medium );
      first = false;
    }
    writeOpeningBrace( writer, indent, null );
    writeBreak( writer, indent );

    writeBodyElements( writer, media.getBodyElements(), indent + 1 );

    writeClosingBrace( writer, indent );
  }

  private void writeKeyframes( final Writer writer, final Keyframes media, final int indent )
    throws IOException
  {
    writer.write( media.getName() );

    writeOpeningBrace( writer, indent, null );
    writeBreak( writer, indent );

    writeBodyElements( writer, media.getBodyElements(), indent + 1 );

    writeClosingBrace( writer, indent );
  }

  private void writeRuleSet( final Writer writer, final RuleSet ruleSet, final int indent )
    throws IOException
  {
    // Don't write rule sets with empty bodies
    final List<DeclarationElement> declarations = ruleSet.getDeclarations();
    if ( null == declarations || 0 == declarations.size() )
    {
      return;
    }

    if ( ruleSet.getArguments().size() > 0 )
    {
      return;
    }

    for ( int idx = 0, selectorsSize = ruleSet.getSelectors().size(); idx < selectorsSize; idx++ )
    {
      final Selector selector = ruleSet.getSelectors().get( idx );
      if ( idx > 0 )
      {
        writeSeparator( writer, "," );
      }
      writer.write( selector.getText() );
    }

    writeOpeningBrace( writer, indent, declarations );
    writeDeclarationBraceSpace( writer, declarations );

    writeDeclarations( writer, declarations, indent );

    writeDeclarationBraceSpace( writer, declarations );

    if ( !isOneLineDeclarationList( declarations ) )
    {
      writeIndent( writer, indent );
    }
    writeClosingBrace( writer, 0 );
  }

  private void writeDeclarations( final Writer writer,
                                  final List<DeclarationElement> declarations,
                                  final int indent )
    throws IOException
  {
    final boolean oneLineDeclarationList = isOneLineDeclarationList( declarations );

    boolean first = true;
    for ( final DeclarationElement declaration : declarations )
    {
      if ( declaration instanceof Declaration )
      {
        if ( !first )
        {
          writeBreak( writer );
        }

        int declarationIndent = indent + 1;
        if ( oneLineDeclarationList )
        {
          declarationIndent = 0;
        }
        writeDeclaration( writer, (Declaration) declaration, declarationIndent );

        first = false;
      }
    }
  }

  private void writeOpeningBrace( final Writer writer, final int indent, final List<DeclarationElement> declarations )
    throws IOException
  {
    if ( isPrettyPrintEnabled() &&
         getPrettyPrintOptions().isOpeningBraceOnNewLine() &&
         !isOneLineDeclarationList( declarations ) )
    {
      writeBreak( writer, indent );
    }
    else
    {
      writeSpace( writer );
    }
    writer.write( '{' );
  }

  private void writeClosingBrace( final Writer writer, final int indent )
    throws IOException
  {
    writer.write( "}" );
    writeBreak( writer, indent );
  }

  private void writeDeclaration( final Writer writer, final Declaration declaration, final int indent )
    throws IOException
  {
    writeIndent( writer, indent );
    writer.write( declaration.getProperty() );
    writer.write( ':' );
    writeSpace( writer );
    for ( final Object value : declaration.getValues() )
    {
      writer.write( value.toString() );
    }
    if ( declaration.isImportant() )
    {
      writeSpace( writer );
      writer.write( "!important" );
    }
    writeSemi( writer, false );
  }

  private void writeDeclarationBraceSpace( final Writer writer, final List<DeclarationElement> declarations )
    throws IOException
  {
    if ( isOneLineDeclarationList( declarations ) )
    {
      writeSpace( writer );
    }
    else
    {
      writeBreak( writer );
    }
  }

  private boolean isOneLineDeclarationList( final List<DeclarationElement> declarations )
  {
    return null != declarations &&
           isPrettyPrintEnabled() &&
           getPrettyPrintOptions().isSingleDeclarationOnOneLine() &&
           1 >= declarations.size();
  }
}
