package com.portfolio.generator.utilities.http;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class HttpUtils implements IHttpUtils {
  @Override
  public String convertEntityToString(final HttpEntity httpEntity) throws IOException {
    Validate.notNull(httpEntity, "HttpEntity was null!");
    return EntityUtils.toString(httpEntity);
  }
}
