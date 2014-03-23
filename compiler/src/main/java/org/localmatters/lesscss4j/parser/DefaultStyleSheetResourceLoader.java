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

import java.net.URL;
import javax.annotation.Nonnull;

public class DefaultStyleSheetResourceLoader
  implements StyleSheetResourceLoader
{
  @Nonnull
  public StyleSheetResource getResource( @Nonnull final URL url )
  {
    if ( "file".equals( url.getProtocol() ) )
    {
      return new FileStyleSheetResource( url.getPath() );
    }
    else
    {
      return new UrlStyleSheetResource( url );
    }
  }
}