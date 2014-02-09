What is LessCSS4j?
------------------

LessCSS4j is a java implementation of the LESS language (http://lesscss.org).
This version has all of the features of the original plus a few additional
features:

* Support for Internet Explorer CSS oddities without the need for escaping
* Pluggable architecture to allow additional functions and transformations
  to be provided.
* Optional Servlet API integration

History
-------

The original implementation of Less was written in Ruby.  This turned out to to
run much too slowly for our purposes when run using JRuby. To solve this
problem, LessCSS4j was written.

While Less has since been reimplemented (less.js) in JavaScript and performs
much better (Using NodeJS), there is still is a need for a Java version to ease
the integration with existing web frameworks.


Licensing
---------

See LICENSE for more information


Command Line Usage
------------------

LESS files can be compiled on the command line using the following:

    $ jlessc.sh myfile.less

This will produce the file `myfile.css` in the same directory as `myfile.less`.

An optional output filename may be provided. If the output filename is `-`, the
output is written to standard out.

Use the `--help` or `-h` option for all of the options accepted.


Servlet Usage
-------------

LessCSS4j provides a servlet to perform runtime compilation of LESS files.  To
use it, add the following servlet to your `web.xml` file (shown with the default
init parameter values):

    <servlet>
        <servlet-name>LessCssServlet</servlet-name>
        <servlet-class>org.lesscss4j.servlet.LessCssServlet</servlet-class>
        <init-param>
            <!-- The amount of time to cache the compiled Less file
                 -1 = cache forever -->
            <param-name>cacheMilliseconds</param-name>
            <param-value>-1</param-value>
        </init-param>
        <init-param>
            <!-- The amount of time the browser should cache the resulting
                 CSS file -->
            <param-name>httpCacheMilliseconds</param-name>
            <param-value>900000</param-value>
        </init-param>
        <init-param>
            <!-- If set to 'true', the resulting CSS will be formatted. Otherwise
                 the resulting CSS is minified -->
            <param-name>prettyPrint</param-name>
            <param-value>false</param-value>
        </init-param>
    </servlet>

    <servlet-mapping>
        <servlet-name>LessCssServlet</servlet-name>
        <url-pattern>/less/*</url-pattern>
    </servlet-mapping>

With this configuration, a request for the url `/less/path/to/file.less` will
look for the file `/path/to/file.less` in the servlet context, compile it using
the LessCSS4j compiler and return the resulting CSS.  In addition, the result
is optionally cached to avoid recompilation.

Embedded Compiler Usage
-----------------------

The easiest way to embed the LessCSS compiler is shown in the following code
snippet:

    StyleSheetResource resource = new FileStyleSheetResource(filename);
    LessCssCompiler compiler = new DefaultLessCssCompilerFactory().create();
    compiler.compile(resource, System.out, null);
