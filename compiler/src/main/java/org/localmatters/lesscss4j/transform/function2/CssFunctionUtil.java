package org.localmatters.lesscss4j.transform.function2;

import java.lang.reflect.Method;
import java.util.Map;
import org.localmatters.lesscss4j.transform.function.Function;
import org.realityforge.css.lizard.CssFunctionProcessor;

public final class CssFunctionUtil
{
  private CssFunctionUtil()
  {
  }

  public static Map<String, Function> toFunctionMap( final Class<?> typeClass )
  {
    try
    {
      final Object instance = typeClass.newInstance();
      final String factoryClassname = typeClass.getName() + CssFunctionProcessor.CLASS_SUFFIX;
      final Class<?> factoryClass = Class.forName( factoryClassname );
      final Object factoryInstance = factoryClass.newInstance();
      final Method toFunctionMapMethod = factoryClass.getMethod( "toFunctionMap", typeClass );
      return (Map<String, Function>) toFunctionMapMethod.invoke( factoryInstance, instance );
    }
    catch ( final Exception e )
    {
      throw new IllegalStateException( "Unable to register functions. Preprocessor incorrectly configured?", e );
    }
  }
}
