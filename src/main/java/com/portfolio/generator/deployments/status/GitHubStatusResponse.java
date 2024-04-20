package com.portfolio.generator.deployments.status;

public class GitHubStatusResponse {
  private String html_url;
  private String status;

  public String getHtml_url() {
    return html_url;
  }

  public void setHtml_url(final String html_url) {
    this.html_url = html_url;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(final String status) {
    this.status = status;
  }
}
