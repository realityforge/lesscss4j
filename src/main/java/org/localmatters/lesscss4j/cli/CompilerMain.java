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

package org.localmatters.lesscss4j.cli;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.localmatters.lesscss4j.compile.DefaultLessCssCompilerFactory;
import org.localmatters.lesscss4j.compile.LessCssCompiler;
import org.localmatters.lesscss4j.output.PrettyPrintOptions;
import org.localmatters.lesscss4j.parser.FileStyleSheetResource;
import org.localmatters.lesscss4j.parser.InputStreamStyleSheetResource;
import org.localmatters.lesscss4j.parser.StyleSheetResource;
import org.realityforge.getopt4j.CLArgsParser;
import org.realityforge.getopt4j.CLOption;
import org.realityforge.getopt4j.CLOptionDescriptor;
import org.realityforge.getopt4j.CLUtil;

/**
 * Entry point for the command line execution of the LessCSS compiler.
 */
public class CompilerMain
{
  private static final int HELP_OPT = 'h';
  private static final int VERSION_OPT = 'v';
  private static final int FORMAT_OPT = 'f';
  private static final int NO_FORMAT_OPT = 1;
  private static final int INDENT_OPT = 'i';
  private static final int LINE_BREAK_OPT = 'l';
  private static final int NO_LINE_BREAK_OPT = 2;
  private static final int SINGLE_LINE_OPT = 's';
  private static final int NO_SINGLE_LINE_OPT = 3;
  private static final int BRACE_NEWLINE_OPT = 'b';
  private static final int NO_BRACE_NEWLINE_OPT = 4;

  private static final CLOptionDescriptor[] OPTIONS = new CLOptionDescriptor[]{
    new CLOptionDescriptor( "help",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            HELP_OPT,
                            "Display this help message" ),
    new CLOptionDescriptor( "version",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            VERSION_OPT,
                            "Display the version" ),
    new CLOptionDescriptor( "format",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            FORMAT_OPT,
                            "Format (pretty print) the CSS file" ),
    new CLOptionDescriptor( "no-format",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            NO_FORMAT_OPT,
                            "Don't format (pretty print) the CSS file" ),
    new CLOptionDescriptor( "indent",
                            CLOptionDescriptor.ARGUMENT_REQUIRED,
                            INDENT_OPT,
                            "Indent VALUE spaces" ),
    new CLOptionDescriptor( "line-break",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            LINE_BREAK_OPT,
                            "Place a blank line between CSS rule sets" ),
    new CLOptionDescriptor( "no-line-break",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            NO_LINE_BREAK_OPT,
                            "Don't place a blank line between CSS rule sets" ),
    new CLOptionDescriptor( "single-line",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            SINGLE_LINE_OPT,
                            "Place single declarations rule sets on one line" ),
    new CLOptionDescriptor( "no-single-line",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            NO_SINGLE_LINE_OPT,
                            "Don't place single declaration rule sets on one line" ),
    new CLOptionDescriptor( "brace-newline",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            BRACE_NEWLINE_OPT,
                            "Place opening braces on their own line" ),
    new CLOptionDescriptor( "no-brace-newline",
                            CLOptionDescriptor.ARGUMENT_DISALLOWED,
                            NO_BRACE_NEWLINE_OPT,
                            "Don't place opening braces on their own line" ),
  };

  private boolean _prettyPrint = false;
  private PrettyPrintOptions _prettyPrintOptions;
  private String _inputFilename;
  private String _outputFilename;

  PrettyPrintOptions getPrettyPrintOptions()
  {
    return _prettyPrintOptions;
  }

  void setPrettyPrintOptions( final PrettyPrintOptions prettyPrintOptions )
  {
    _prettyPrintOptions = prettyPrintOptions;
  }

  boolean isPrettyPrint()
  {
    return _prettyPrint;
  }

  void setPrettyPrint( final boolean prettyPrint )
  {
    _prettyPrint = prettyPrint;
  }

  private void printUsage()
  {
    final String lineSeparator = System.getProperty( "line.separator" );

    final StringBuilder msg = new StringBuilder();

    msg.append( "Usage: " );
    msg.append( CompilerMain.class.getName() );
    msg.append( " [options] [input] [output]" );
    msg.append( lineSeparator );
    msg.append( "Options: " );
    msg.append( lineSeparator );

    msg.append( CLUtil.describeOptions( OPTIONS ).toString() );

    System.out.println( msg.toString() );
  }

  void printVersion()
  {
    System.out.println( "jlessc <todo:VERSION>" );
  }

  int run( final String[] args )
  {
    if ( !processOptions( args ) )
    {
      return -1;
    }

    try
    {
      compile();
    }
    catch ( final Throwable t )
    {
      System.err.println( t.toString() );
      return -1;
    }

    return 0;
  }

  private boolean processOptions( final String[] args )
  {
    // Parse the arguments
    final CLArgsParser parser = new CLArgsParser( args, OPTIONS );

    //Make sure that there was no errors parsing arguments
    if ( null != parser.getErrorString() )
    {
      System.err.println( parser.getErrorString() );
      return false;
    }
    final PrettyPrintOptions formatOptions = new PrettyPrintOptions();

    // Get a list of parsed options
    final List<CLOption> options = parser.getArguments();
    for ( final CLOption option : options )
    {
      switch ( option.getId() )
      {
        case CLOption.TEXT_ARGUMENT:
        {
          if ( null == _inputFilename )
          {
            _inputFilename = option.getArgument();
          }
          else if ( null == _outputFilename )
          {
            _outputFilename = option.getArgument();
          }
          else
          {
            System.err.println( "Unknown parameter specified: " + option.getArgument() );
            return false;
          }
          break;
        }
        case FORMAT_OPT:
        {
          setPrettyPrint( true );
          break;
        }
        case NO_FORMAT_OPT:
        {
          setPrettyPrint( false );
          break;
        }
        case LINE_BREAK_OPT:
        {
          formatOptions.setLineBetweenRuleSets( true );
          break;
        }
        case NO_LINE_BREAK_OPT:
        {
          formatOptions.setLineBetweenRuleSets( false );
          break;
        }
        case SINGLE_LINE_OPT:
        {
          formatOptions.setSingleDeclarationOnOneLine( true );
          break;
        }
        case NO_SINGLE_LINE_OPT:
        {
          formatOptions.setSingleDeclarationOnOneLine( false );
          break;
        }
        case BRACE_NEWLINE_OPT:
        {
          formatOptions.setOpeningBraceOnNewLine( true );
          break;
        }
        case NO_BRACE_NEWLINE_OPT:
        {
          formatOptions.setOpeningBraceOnNewLine( false );
          break;
        }
        case INDENT_OPT:
        {
          final String indent = option.getArgument();
          try
          {
            formatOptions.setIndentSize( Integer.parseInt( indent ) );
          }
          catch ( final NumberFormatException nfe )
          {
            System.err.println( "Invalid indent value: " + indent );
            return false;
          }
          break;
        }
        case VERSION_OPT:
        {
          printVersion();
          return false;
        }
        case HELP_OPT:
        {
          printUsage();
          return false;
        }
      }
    }
    setPrettyPrintOptions( formatOptions );

    return true;
  }

  void compile()
  {
    compile( _inputFilename, _outputFilename );
  }

  void compile( final String inputFilename, String outputFilename )
  {
    OutputStream output = null;
    boolean outputFileExisted = false;
    File outputFile = null;
    try
    {
      // todo: verify that inputFilename and outputFilename don't correspond to directories

      // Generate an output filename from the input filename
      if ( null == outputFilename && null != inputFilename )
      {
        outputFilename = generateOutputFilename( inputFilename );
      }

      if ( null != outputFilename )
      {
        outputFile = new File( outputFilename );
        if ( outputFile.exists() )
        {
          outputFileExisted = true;
        }
      }

      final StyleSheetResource input = createInput( inputFilename );
      output = createOutputStream( outputFilename );

      final DefaultLessCssCompilerFactory factory = new DefaultLessCssCompilerFactory();
      factory.setPrettyPrintEnabled( isPrettyPrint() );

      if ( isPrettyPrint() && null != getPrettyPrintOptions() )
      {
        factory.setPrettyPrintOptions( getPrettyPrintOptions() );
      }

      final LessCssCompiler compiler = factory.create();
      compiler.compile( input, output, null );
    }
    catch ( final IOException ioe )
    {
      // delete the bogus output file if we're not writing to stdout and it didn't exist before.
      if ( null != outputFile && !outputFileExisted )
      {
        FileUtils.deleteQuietly( outputFile );
      }
    }
    finally
    {
      IOUtils.closeQuietly( output );
    }
  }

  OutputStream createOutputStream( final String outputFilename )
    throws IOException
  {
    if ( outputFilename == null || "-".equals( outputFilename ) )
    {
      return System.out;
    }
    else
    {
      return FileUtils.openOutputStream( new File( outputFilename ) );
    }
  }

  StyleSheetResource createInput( final String inputFilename )
  {
    if ( inputFilename == null || inputFilename.equals( "-" ) )
    {
      return new InputStreamStyleSheetResource( System.in );
    }
    else
    {
      return new FileStyleSheetResource( inputFilename );
    }
  }

  private String generateOutputFilename( final String inputFilename )
  {
    final String extension = FilenameUtils.getExtension( inputFilename );

    final StringBuilder outputFilename = new StringBuilder();
    outputFilename.append( inputFilename, 0, inputFilename.length() - extension.length() );
    if ( outputFilename.charAt( outputFilename.length() - 1 ) == '.' )
    {
      outputFilename.deleteCharAt( outputFilename.length() - 1 );
    }

    // Don't want to clobber the existing css file.
    if ( extension.equals( "css" ) )
    {
      outputFilename.append( "-min" );
    }

    outputFilename.append( ".css" );

    return outputFilename.toString();
  }

  public static void main( final String[] args )
  {
    System.exit( new CompilerMain().run( args ) );
  }
}
