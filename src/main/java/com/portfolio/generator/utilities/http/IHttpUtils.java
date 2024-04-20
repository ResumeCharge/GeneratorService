package com.portfolio.generator.utilities.http;

import org.apache.http.HttpEntity;

import java.io.IOException;

public interface IHttpUtils {
  String convertEntityToString(final HttpEntity httpEntity) throws IOException;
}
