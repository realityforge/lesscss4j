require 'buildr/git_auto_version'
require 'buildr/single_intermediate_layout'
require 'buildr/top_level_generate_dir'
require 'buildr/antlr'
require 'buildr/jacoco'
require 'buildr/gpg'

PROVIDED_DEPS = [:javax_javaee, :javax_servlet, :javax_jsr305]
COMPILE_DEPS = [Buildr::Antlr.runtime_dependencies, :commons_io, :getopt4j]
TEST_DEPS = [:mockito]

desc 'Less CSS for Java'
define 'lesscss4j' do
  project.group = 'org.localmatters'
  compile.options.source = '1.7'
  compile.options.target = '1.7'
  compile.options.lint = 'all'

  project.version = ENV['PRODUCT_VERSION'] if ENV['PRODUCT_VERSION']

  pom.add_apache2_license
  pom.add_github_project('realityforge/lesscss4j')
  pom.add_developer('realityforge', 'Peter Donald', 'peter@realityforge.org')
  pom.add_developer('dhay', 'David Hay', 'hay_dave@yahoo.com')
  pom.provided_dependencies.concat PROVIDED_DEPS
  pom.description = 'Java-based "compiler" for the Less CSS language. Compiles LESS files into standard CSS and provides utility classes for integration with existing web application.'

  define 'model' do
    compile.with :javax_jsr305

    test.with TEST_DEPS
    test.using :testng

    package(:jar)
  end

  define 'compiler' do

    compile.with PROVIDED_DEPS, COMPILE_DEPS, project('model')

    compile.from compile_antlr(_('src/main/antlr3/org/localmatters/lesscssj4/parser/LessCss.g'), :package => 'org.localmatters.lesscss4j.parser.antlr')

    test.with TEST_DEPS
    test.using :testng

    package(:jar)
    package(:sources)
    package(:javadoc)
  end

  ipr.extra_modules << '../less.js/less.js.iml'

  ipr.add_component("CompilerConfiguration") do |xml|
    xml.option :name => 'DEFAULT_COMPILER', :value => 'Javac'
    xml.wildcardResourcePatterns do |xml|
      xml.entry :name => '!?*.java'
      xml.entry :name => '!?*.class'
    end
    xml.annotationProcessing do |xml|
      xml.profile(:default => true, :name => 'Default', :enabled => true) do |xml|
        xml.sourceOutputDir(:name => 'generated/ap/main/java')
        xml.sourceTestOutputDir(:name => 'generated/ap/test/java')
        xml.outputRelativeToContentRoot(:value => true)
        #xml.processor(:name => 'org.localmatters.lesscss4j.transform.function2.CssFunctionProcessor')
        xml.processorPath(:useClasspath => true)
      end
    end
  end
end
