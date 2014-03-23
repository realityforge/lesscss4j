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
package org.localmatters.lesscss4j.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.localmatters.lesscss4j.util.Hex;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class LessCssServletTest
{
  LessCssServlet _servlet;
  HttpServletRequest _request;
  HttpServletResponse _response;
  ServletContext _servletContext;
  MockServletConfig _servletConfig;
  final String _path = "less/tiny.less";
  URL _url;
  byte[] _cssBytes;
  String _cssStr;
  long _systemMillis;

  @BeforeMethod
  protected void setUp()
    throws Exception
  {
    _request = mock( HttpServletRequest.class );
    _response = mock( HttpServletResponse.class );
    _servletContext = mock( ServletContext.class );

    _servletConfig = new MockServletConfig(_servletContext);

    _systemMillis = System.currentTimeMillis();

    _servlet = new LessCssServlet()
    {
      @Override
      protected long getTime()
      {
        return _systemMillis;
      }
    };

    _url = getClass().getClassLoader().getResource( _path );

    final InputStream input = _url.openStream();
    try
    {
      _cssBytes = IOUtils.toByteArray( input );
      _cssStr = new String( _cssBytes, "UTF-8" );
    }
    finally
    {
      IOUtils.closeQuietly( input );
    }
  }

  @Test
  public void emptyCacheValidResource()
    throws IOException, ServletException
  {
    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "GET" );
    when( _request.getDateHeader( LessCssServlet.IF_MOD_SINCE ) ).thenReturn( -1L );
    when( _request.getHeader( LessCssServlet.IF_NONE_MATCH ) ).thenReturn( null );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );

    when( _servletContext.getResource( _path ) ).thenReturn( _url );

    _response.addDateHeader( LessCssServlet.LAST_MODIFIED, _systemMillis );
    _response.addDateHeader( LessCssServlet.EXPIRES, _systemMillis + 900000 );
    _response.addHeader( LessCssServlet.CACHE_CONTROL, "max-age=900" );
    _response.setContentType( "text/css; charset=UTF-8" );
    _response.setContentLength( _cssBytes.length );

    final MockServletOutputStream responseStream = new MockServletOutputStream();
    when( _response.getOutputStream() ).thenReturn( responseStream );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );

    assertEquals( responseStream.asString(), _cssStr );
  }

  @Test
  public void cachedResource()
    throws IOException, ServletException
  {
    emptyCacheValidResource();

    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "GET" );
    when( _request.getDateHeader( LessCssServlet.IF_MOD_SINCE ) ).thenReturn( -1L );
    when( _request.getHeader( LessCssServlet.IF_NONE_MATCH ) ).thenReturn( null );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );


    _response.addDateHeader( LessCssServlet.LAST_MODIFIED, _systemMillis );
    _response.addDateHeader( LessCssServlet.EXPIRES, _systemMillis + 900000 );
    _response.addHeader( LessCssServlet.CACHE_CONTROL, "max-age=900" );
    _response.setContentType( "text/css; charset=UTF-8" );
    _response.setContentLength( _cssBytes.length );

    final MockServletOutputStream responseStream = new MockServletOutputStream();
    when( _response.getOutputStream() ).thenReturn( responseStream );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );

    assertEquals( responseStream.asString(), _cssStr );
}

  @Test
  public void cachedResourceETag()
    throws IOException, ServletException
  {
    emptyCacheValidResource();

    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "GET" );
    when( _request.getHeader( LessCssServlet.IF_NONE_MATCH ) ).thenReturn( Hex.md5( _cssBytes ) );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );

    _response.setStatus( HttpServletResponse.SC_NOT_MODIFIED );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );
}

  @Test
  public void cachedResourceETagNeedsRefresh()
    throws IOException, ServletException
  {
    _servlet.setCacheMillis( 10 );

    emptyCacheValidResource();

    _systemMillis = _systemMillis - 20;

    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "GET" );
    when( _request.getHeader( LessCssServlet.IF_NONE_MATCH ) ).thenReturn( "bogus" );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );

    when( _servletContext.getRealPath( _path ) ).thenReturn( _path );
    when( _servletContext.getResource( _path ) ).thenReturn( _url );

    _response.addDateHeader( LessCssServlet.LAST_MODIFIED, _systemMillis );
    _response.addDateHeader( LessCssServlet.EXPIRES, _systemMillis + 900000 );
    _response.addHeader( LessCssServlet.CACHE_CONTROL, "max-age=900" );
    _response.setContentType( "text/css; charset=UTF-8" );
    _response.setContentLength( _cssBytes.length );

    final MockServletOutputStream responseStream = new MockServletOutputStream();
    when( _response.getOutputStream() ).thenReturn( responseStream );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );

    assertEquals( responseStream.asString(), _cssStr );
}

  @Test
  public void cachedResourceNeedsRefresh()
    throws IOException, ServletException
  {
    _servlet.setCacheMillis( 10 );

    emptyCacheValidResource();

    _systemMillis = _systemMillis - 20;

    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "GET" );
    when( _request.getDateHeader( LessCssServlet.IF_MOD_SINCE ) ).thenReturn( -1L );
    when( _request.getHeader( LessCssServlet.IF_NONE_MATCH ) ).thenReturn( null );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );

    when( _servletContext.getRealPath( _path ) ).thenReturn( _path );
    when( _servletContext.getResource( _path ) ).thenReturn( _url );

    _response.addDateHeader( LessCssServlet.LAST_MODIFIED, _systemMillis );
    _response.addDateHeader( LessCssServlet.EXPIRES, _systemMillis + 900000 );
    _response.addHeader( LessCssServlet.CACHE_CONTROL, "max-age=900" );
    _response.setContentType( "text/css; charset=UTF-8" );
    _response.setContentLength( _cssBytes.length );

    final MockServletOutputStream responseStream = new MockServletOutputStream();
    when( _response.getOutputStream() ).thenReturn( responseStream );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );

    assertEquals( responseStream.asString(), _cssStr );
}

  @Test
  public void cachedResourceNotModified()
    throws IOException, ServletException
  {
    emptyCacheValidResource();

    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "GET" );
    when( _request.getDateHeader( LessCssServlet.IF_MOD_SINCE ) ).thenReturn( _systemMillis + 1000 );
    when( _request.getHeader( LessCssServlet.IF_NONE_MATCH ) ).thenReturn( null );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );

    _response.setStatus( HttpServletResponse.SC_NOT_MODIFIED );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );
}

  @Test
  public void cachedResourceHeadRequest()
    throws IOException, ServletException
  {
    emptyCacheValidResource();

    when( _request.getPathInfo() ).thenReturn( _path );
    when( _request.getMethod() ).thenReturn( "HEAD" );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );


    _response.addDateHeader( LessCssServlet.LAST_MODIFIED, _systemMillis );
    _response.addDateHeader( LessCssServlet.EXPIRES, _systemMillis + 900000 );
    _response.addHeader( LessCssServlet.CACHE_CONTROL, "max-age=900" );
    _response.setContentType( "text/css; charset=UTF-8" );
    _response.setContentLength( _cssBytes.length );

    _servlet.init( _servletConfig );
    _servlet.service( _request, _response );
}

  @Test
  public void NullPath()
    throws IOException, ServletException
  {
    when( _request.getPathInfo() ).thenReturn( null );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );
    _response.sendError( HttpServletResponse.SC_NOT_FOUND );

    _servlet.service( _request, _response );
  }

  @Test
  public void EmptyPath()
    throws IOException, ServletException
  {
    when( _request.getPathInfo() ).thenReturn( "  " );
    when( _request.getParameter( LessCssServlet.CLEAR_CACHE ) ).thenReturn( null );
    _response.sendError( HttpServletResponse.SC_NOT_FOUND );

    _servlet.service( _request, _response );
  }

  private static class MockServletConfig
    implements ServletConfig
  {
    private final ServletContext _servletContext;
    private final Map<String, String> _initParameters = new LinkedHashMap<String, String>();

    private MockServletConfig( @Nonnull final ServletContext servletContext )
    {
      _servletContext = servletContext;
    }

    public String getServletName()
    {
      return "";
    }

    public ServletContext getServletContext()
    {
      return _servletContext;
    }

    public String getInitParameter( final String name )
    {
      return _initParameters.get( name );
    }

    public Enumeration<String> getInitParameterNames()
    {
      return new Enumeration<String>()
      {
        final Iterator<String> _iter = _initParameters.keySet().iterator();

        public boolean hasMoreElements()
        {
          return _iter.hasNext();
        }

        public String nextElement()
        {
          return _iter.next();
        }
      };
    }
  }

  private static class MockServletOutputStream
    extends ServletOutputStream
  {
    private final ByteArrayOutputStream _output = new ByteArrayOutputStream();

    @Override
    public void write( final int b )
      throws IOException
    {
      _output.write( b );
    }

    public String asString()
      throws UnsupportedEncodingException
    {
      return asString( "UTF-8" );
    }

    public String asString( final String charsetName )
      throws UnsupportedEncodingException
    {
      return new String( _output.toByteArray(), charsetName );
    }

    @Override
    public boolean isReady()
    {
      return true;
    }

    @Override
    public void setWriteListener( final WriteListener writeListener )
    {
    }
  }
}

