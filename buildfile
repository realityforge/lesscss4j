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

  compile.with PROVIDED_DEPS, COMPILE_DEPS

  compile.from compile_antlr(_('src/main/antlr3/org/localmatters/lesscssj4/parser/LessCss.g'), :package => 'org.localmatters.lesscss4j.parser.antlr')

  test.with :junit, :mockito

  package(:bundle).tap do |bnd|
    bnd['Import-Package'] = 'org.antlr*;version="3.2",javax.servlet*; resolution:=optional,*'
    bnd['Export-Package'] = "gelf4j.*;version=#{version}"
  end
end
