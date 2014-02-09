require 'buildr/git_auto_version'
require 'buildr/top_level_generate_dir'
require 'buildr/antlr'
require 'buildr/bnd'
require 'buildr/jacoco'

PROVIDED_DEPS = [:javax_javaee, :javax_servlet]
COMPILE_DEPS = [Buildr::Antlr.runtime_dependencies, :commons_io]

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

  compile.with PROVIDED_DEPS, COMPILE_DEPS

  compile.from compile_antlr(_('src/main/antlr3/org/localmatters/lesscssj4/parser/LessCss.g'), :package => 'org.localmatters.lesscss4j.parser.antlr')

  test.with :junit, :mockito

  package(:bundle).tap do |bnd|
    bnd['Import-Package'] = 'org.antlr*;version="3.2",javax.servlet*; resolution:=optional,*'
    bnd['Export-Package'] = "org.localmatters.lesscss4j.*;version=#{version}"
  end
  package(:bundle, :classifier => 'all').tap do |bnd|
    bnd['Main-Class'] = 'org.localmatters.lesscss4j.cli.CompilerMain'
    bnd['Bundle-SymbolicName'] = "#{project.group}.#{project.id}-all"
    bnd['Private-Package'] = 'org.antlr*,org.apache.commons.io.*'
    bnd['Export-Package'] = "org.localmatters.lesscss4j.*;version=#{version}"
  end
end
