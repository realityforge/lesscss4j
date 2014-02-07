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
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.localmatters.lesscss4j.compile.DefaultLessCssCompilerFactory;
import org.localmatters.lesscss4j.compile.LessCssCompiler;
import org.localmatters.lesscss4j.output.PrettyPrintOptions;
import org.localmatters.lesscss4j.parser.FileStyleSheetResource;
import org.localmatters.lesscss4j.parser.InputStreamStyleSheetResource;
import org.localmatters.lesscss4j.parser.StyleSheetResource;

/**
 * Entry point for the command line execution of the LessCSS compiler.
 */
public class CompilerMain {
    public boolean _prettyPrint = false;
    public PrettyPrintOptions _prettyPrintOptions;
    private String _inputFilename;
    private String _outputFilename;

    public PrettyPrintOptions getPrettyPrintOptions() {
        return _prettyPrintOptions;
    }

    public void setPrettyPrintOptions(PrettyPrintOptions prettyPrintOptions) {
        _prettyPrintOptions = prettyPrintOptions;
    }

    public boolean isPrettyPrint() {
        return _prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        _prettyPrint = prettyPrint;
    }

    protected void usage() {
        System.out.println("Usage: jlessc [options] [input] [output]");
        System.out.println("  -h,  --help               Display this help message");
        System.out.println("  -v,  --version            Display the version");
        System.out.println("  -f,  --format             Format (pretty print) the CSS file");
        System.out.println("  -nf, --no-format          Don't format (pretty print) the CSS file");
        System.out.println("  -i,  --indent <value>     Indent VALUE spaces");
        System.out.println("  -l,  --line-break         Place a blank line between CSS rule sets");
        System.out.println("  -nl, --no-line-break      Don't place a blank line between CSS rule sets");
        System.out.println("  -s,  --single-line        Place single declarations rulesets on one line");
        System.out.println("  -ns, --no-single-line     Don't place single declaration rulesets on one line");
        System.out.println("  -b,  --brace-newline      Place opening braces on their own line");
        System.out.println("  -nb  --no-brace-newline   Don't place opening braces on their own line");
    }

    protected void version() {
        System.out.println("jlessc <todo:VERSION>");
    }

    protected boolean isOption(String arg, String... options) {
        for (String option : options) {
            if (option.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the argument is an "anti" option, meaning that is disables a regular option.  If the regular options
     * are --something or -s, the "anti" options are --no-something or -ns.
     * @param arg
     * @return
     */
    protected boolean isAntiOption(String arg) {
        return arg.startsWith("--no") || (arg.startsWith("-n") && arg.length() >= 3);
    }


    public int run(String[] args) {

        int result = parseCommandLine(args);
        if (result != 0) {
            return result;
        }

        try {
            compile();
        }
        catch (IOException io) {
            System.err.println(io.toString());
            return -1;
        }

        return 0;
    }

    protected int parseCommandLine(String[] args) {
        boolean argsFinished = false;

        PrettyPrintOptions formatOptions = new PrettyPrintOptions();

        for (int idx = 0; idx < args.length; idx++) {
            String arg = args[idx];
            if (arg.equals("--")) {
                argsFinished = true;
            }
            if (!argsFinished && isOption(arg, "--help", "-h")) {
                usage();
                return 1;
            }
            else if (!argsFinished && isOption(arg, "--version", "-v")) {
                version();
            }
            else if (!argsFinished && isOption(arg, "--format", "-f", "--no-format", "-nf")) {
                setPrettyPrint(!isAntiOption(arg));
            }
            else if (!argsFinished && isOption(arg, "--indent", "-i")) {
                String indent = args[++idx];
                try {
                    formatOptions.setIndentSize(Integer.parseInt(indent));
                }
                catch (NumberFormatException ex) {
                    System.err.println("Invalid indent value: " + indent);
                    return -1;
                }
            }
            else if (!argsFinished && isOption(arg, "--line-break", "-l", "--no-line-break", "-nl")) {
                formatOptions.setLineBetweenRuleSets(!isAntiOption(arg));
            }
            else if (!argsFinished && isOption(arg, "--single-line", "-s", "--no-single-line", "-ns")) {
                formatOptions.setSingleDeclarationOnOneLine(!isAntiOption(arg));
            }
            else if (!argsFinished && isOption(arg, "--brace-newline", "-b", "--no-brace-newline", "-nb")) {
                formatOptions.setOpeningBraceOnNewLine(!isAntiOption(arg));
            }
            else if (!argsFinished &&
                     (arg.startsWith("--") || (arg.startsWith("-") && arg.length() > 1))) {
                System.err.println("Unknown option: " + arg);
            }
            else{
                if (_inputFilename == null) {
                    _inputFilename = arg;
                }
                else if (_outputFilename == null) {
                    _outputFilename = arg;
                }
                else {
                    // error
                }
            }
        }

        setPrettyPrintOptions(formatOptions);

        return 0;
    }

    public void compile() throws IOException {
        compile(_inputFilename, _outputFilename);
    }

    public void compile(String inputFilename, String outputFilename) throws IOException {
        StyleSheetResource input;
        OutputStream output = null;
        boolean outputFileExisted = false;
        File outputFile = null;
        try {
            // todo: verify that inputfilename and outputfilename don't correspond to directories

            URL inputUrl = null;
            if (inputFilename != null) {
                inputUrl = new File(inputFilename).toURI().toURL();
            }

            // Generate an output filename from the input filename
            if (outputFilename == null && inputFilename != null) {
                outputFilename = generateOutputFilename(inputFilename);
            }

            if (outputFilename != null) {
                outputFile = new File(outputFilename);
                if (outputFile.exists()) {
                    outputFileExisted = true;
                }
            }

            input = createInput(inputFilename);
            output = createOutputStream(outputFilename);


            DefaultLessCssCompilerFactory factory = new DefaultLessCssCompilerFactory();
            factory.setPrettyPrintEnabled(isPrettyPrint());

            if (isPrettyPrint() && getPrettyPrintOptions() != null) {
                factory.setPrettyPrintOptions(getPrettyPrintOptions());
            }

            LessCssCompiler compiler = factory.create();
            compiler.compile(input, output, null);
        }
        catch (IOException ex) {
            // delete the bogus output file if we're not writing to stdout and it didn't exist before.
            if (outputFile != null && !outputFileExisted) {
                FileUtils.deleteQuietly(outputFile);
            }
        }
        finally {
            IOUtils.closeQuietly(output);
        }
    }

    protected OutputStream createOutputStream(String outputFilename) throws IOException {
        if (outputFilename == null || "-".equals(outputFilename)) {
            return System.out;
        }
        else {
            return FileUtils.openOutputStream(new File(outputFilename));
        }
    }

    protected StyleSheetResource createInput(String inputFilename) throws IOException {
        if (inputFilename == null || inputFilename.equals("-")) {
            return new InputStreamStyleSheetResource(System.in);
        }
        else {
            return new FileStyleSheetResource(inputFilename);
        }
    }

    protected String generateOutputFilename(String inputFilename) {

        String extension = FilenameUtils.getExtension(inputFilename);

        StringBuilder outputFilename = new StringBuilder();
        outputFilename.append(inputFilename, 0, inputFilename.length() - extension.length());
        if (outputFilename.charAt(outputFilename.length() - 1) == '.') {
            outputFilename.deleteCharAt(outputFilename.length() - 1);
        }

        // Don't want to clobber the existing css file.
        if (extension.equals("css")) {
            outputFilename.append("-min");
        }

        outputFilename.append(".css");

        return outputFilename.toString();
    }

    public static void main(String[] args) {
        int returnValue = new CompilerMain().run(args);
        if (returnValue != 0) {
            System.exit(returnValue);
        }
    }

}
