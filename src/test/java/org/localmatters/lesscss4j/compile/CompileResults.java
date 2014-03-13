package org.localmatters.lesscss4j.compile;

import javax.annotation.Nullable;

final class CompileResults
{
  @Nullable
  private final String _output;
  @Nullable
  private final String _errorOutput;
  private final int _errorCount;

  CompileResults( @Nullable final String output, @Nullable final String errorOutput, final int errorCount )
  {
    _output = output;
    _errorOutput = errorOutput;
    _errorCount = errorCount;
  }

  @Nullable
  String getOutput()
  {
    return _output;
  }

  @Nullable
  String getErrorOutput()
  {
    return _errorOutput;
  }

  int getErrorCount()
  {
    return _errorCount;
  }
}
