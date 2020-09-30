package com.takipi.oss.storage.helper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CrossOriginFilter implements ContainerResponseFilter {
  private static final Logger LOG = LoggerFactory.getLogger(CrossOriginFilter.class);
  
  private static final String ORIGIN_HEADER = "Origin";
  
  public static final String ACCESS_CONTROL_REQUEST_METHOD_HEADER = "Access-Control-Request-Method";
  
  public static final String ACCESS_CONTROL_REQUEST_HEADERS_HEADER = "Access-Control-Request-Headers";
  
  public static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
  
  public static final String ACCESS_CONTROL_ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
  
  public static final String ACCESS_CONTROL_ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
  
  public static final String ACCESS_CONTROL_MAX_AGE_HEADER = "Access-Control-Max-Age";
  
  public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS_HEADER = "Access-Control-Allow-Credentials";
  
  public static final String ACCESS_CONTROL_EXPOSE_HEADERS_HEADER = "Access-Control-Expose-Headers";
  
  public static final String TIMING_ALLOW_ORIGIN_HEADER = "Timing-Allow-Origin";
  
  public static final String ALLOWED_ORIGINS_PARAM = "allowedOrigins";
  
  public static final String ALLOWED_TIMING_ORIGINS_PARAM = "allowedTimingOrigins";
  
  public static final String ALLOWED_METHODS_PARAM = "allowedMethods";
  
  public static final String ALLOWED_HEADERS_PARAM = "allowedHeaders";
  
  public static final String PREFLIGHT_MAX_AGE_PARAM = "preflightMaxAge";
  
  public static final String ALLOW_CREDENTIALS_PARAM = "allowCredentials";
  
  public static final String EXPOSED_HEADERS_PARAM = "exposedHeaders";
  
  public static final String OLD_CHAIN_PREFLIGHT_PARAM = "forwardPreflight";
  
  public static final String CHAIN_PREFLIGHT_PARAM = "chainPreflight";
  
  private static final String ANY_ORIGIN = "*";
  
  private static final String DEFAULT_ALLOWED_ORIGINS = "*";
  
  private static final String DEFAULT_ALLOWED_TIMING_ORIGINS = "";
  
  private static final List<String> SIMPLE_HTTP_METHODS = Arrays.asList(new String[] { "GET", "POST", "HEAD" });
  
  private static final List<String> DEFAULT_ALLOWED_METHODS = Arrays.asList(new String[] { "GET", "POST", "HEAD" });
  
  private static final List<String> DEFAULT_ALLOWED_HEADERS = Arrays.asList(new String[] { "X-Requested-With", "Content-Type", "Accept", "Origin" });
  
  private boolean anyOriginAllowed;
  
  private boolean anyTimingOriginAllowed;
  
  private boolean anyHeadersAllowed;
  
  private List<String> allowedOrigins = new ArrayList<>();
  
  private List<String> allowedTimingOrigins = new ArrayList<>();
  
  private List<String> allowedMethods = new ArrayList<>();
  
  private List<String> allowedHeaders = new ArrayList<>();
  
  private List<String> exposedHeaders = new ArrayList<>();
  
  private int preflightMaxAge;
  
  private boolean allowCredentials;
  
  private boolean chainPreflight;
  
  public CrossOriginFilter(FilterConfig config) {
    String allowedOriginsConfig = config.getInitParameter("allowedOrigins");
    String allowedTimingOriginsConfig = config.getInitParameter("allowedTimingOrigins");
    this.anyOriginAllowed = generateAllowedOrigins(this.allowedOrigins, allowedOriginsConfig, "*");
    this.anyTimingOriginAllowed = generateAllowedOrigins(this.allowedTimingOrigins, allowedTimingOriginsConfig, "");
    String allowedMethodsConfig = config.getInitParameter("allowedMethods");
    if (allowedMethodsConfig == null) {
      this.allowedMethods.addAll(DEFAULT_ALLOWED_METHODS);
    } else {
      this.allowedMethods.addAll(Arrays.asList(StringUtil.csvSplit(allowedMethodsConfig)));
    } 
    String allowedHeadersConfig = config.getInitParameter("allowedHeaders");
    if (allowedHeadersConfig == null) {
      this.allowedHeaders.addAll(DEFAULT_ALLOWED_HEADERS);
    } else if ("*".equals(allowedHeadersConfig)) {
      this.anyHeadersAllowed = true;
    } else {
      this.allowedHeaders.addAll(Arrays.asList(StringUtil.csvSplit(allowedHeadersConfig)));
    } 
    String preflightMaxAgeConfig = config.getInitParameter("preflightMaxAge");
    if (preflightMaxAgeConfig == null)
      preflightMaxAgeConfig = "1800"; 
    try {
      this.preflightMaxAge = Integer.parseInt(preflightMaxAgeConfig);
    } catch (NumberFormatException x) {
      LOG.info("Cross-origin filter, could not parse '{}' parameter as integer: {}", "preflightMaxAge", preflightMaxAgeConfig);
    } 
    String allowedCredentialsConfig = config.getInitParameter("allowCredentials");
    if (allowedCredentialsConfig == null)
      allowedCredentialsConfig = "true"; 
    this.allowCredentials = Boolean.parseBoolean(allowedCredentialsConfig);
    String exposedHeadersConfig = config.getInitParameter("exposedHeaders");
    if (exposedHeadersConfig == null)
      exposedHeadersConfig = ""; 
    this.exposedHeaders.addAll(Arrays.asList(StringUtil.csvSplit(exposedHeadersConfig)));
    String chainPreflightConfig = config.getInitParameter("forwardPreflight");
    if (chainPreflightConfig != null) {
      LOG.warn("DEPRECATED CONFIGURATION: Use chainPreflight instead of forwardPreflight");
    } else {
      chainPreflightConfig = config.getInitParameter("chainPreflight");
    } 
    if (chainPreflightConfig == null)
      chainPreflightConfig = "true"; 
    this.chainPreflight = Boolean.parseBoolean(chainPreflightConfig);
    LOG.debug("Cross-origin filter configuration: allowedOrigins = " + allowedOriginsConfig + ", " + "allowedTimingOrigins" + " = " + allowedTimingOriginsConfig + ", " + "allowedMethods" + " = " + allowedMethodsConfig + ", " + "allowedHeaders" + " = " + allowedHeadersConfig + ", " + "preflightMaxAge" + " = " + preflightMaxAgeConfig + ", " + "allowCredentials" + " = " + allowedCredentialsConfig + "," + "exposedHeaders" + " = " + exposedHeadersConfig + "," + "chainPreflight" + " = " + chainPreflightConfig);
  }
  
  private boolean generateAllowedOrigins(List<String> allowedOriginStore, String allowedOriginsConfig, String defaultOrigin) {
    if (allowedOriginsConfig == null)
      allowedOriginsConfig = defaultOrigin; 
    String[] allowedOrigins = StringUtil.csvSplit(allowedOriginsConfig);
    for (String allowedOrigin : allowedOrigins) {
      if (allowedOrigin.length() > 0) {
        if ("*".equals(allowedOrigin)) {
          allowedOriginStore.clear();
          return true;
        } 
        allowedOriginStore.add(allowedOrigin);
      } 
    } 
    return false;
  }
  
  public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
    handle((HttpServletRequest)request, (HttpServletResponse)response);
  }
  
  private void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String origin = request.getHeader("Origin");
    if (origin != null && isEnabled(request))
      if (this.anyOriginAllowed || originMatches(this.allowedOrigins, origin)) {
        if (isSimpleRequest(request)) {
          LOG.debug("Cross-origin request to {} is a simple cross-origin request", request.getRequestURI());
          handleSimpleResponse(request, response, origin);
        } else if (isPreflightRequest(request)) {
          LOG.debug("Cross-origin request to {} is a preflight cross-origin request", request
              .getRequestURI());
          handlePreflightResponse(request, response, origin);
          if (this.chainPreflight) {
            LOG.debug("Preflight cross-origin request to {} forwarded to application", request
                .getRequestURI());
          } else {
            return;
          } 
        } else {
          LOG.debug("Cross-origin request to {} is a non-simple cross-origin request", request
              .getRequestURI());
          handleSimpleResponse(request, response, origin);
        } 
        if (this.anyTimingOriginAllowed || originMatches(this.allowedTimingOrigins, origin)) {
          response.setHeader("Timing-Allow-Origin", origin);
        } else {
          LOG.debug("Cross-origin request to " + request.getRequestURI() + " with origin " + origin + " does not match allowed timing origins " + this.allowedTimingOrigins);
        } 
      } else {
        LOG.debug("Cross-origin request to " + request.getRequestURI() + " with origin " + origin + " does not match allowed origins " + this.allowedOrigins);
      }  
  }
  
  protected boolean isEnabled(HttpServletRequest request) {
    for (Enumeration<String> connections = request.getHeaders("Connection"); connections.hasMoreElements(); ) {
      String connection = connections.nextElement();
      if ("Upgrade".equalsIgnoreCase(connection))
        for (Enumeration<String> upgrades = request.getHeaders("Upgrade"); upgrades.hasMoreElements(); ) {
          String upgrade = upgrades.nextElement();
          if ("WebSocket".equalsIgnoreCase(upgrade))
            return false; 
        }  
    } 
    return true;
  }
  
  private boolean originMatches(List<String> allowedOrigins, String originList) {
    if (originList.trim().length() == 0)
      return false; 
    String[] origins = originList.split(" ");
    for (String origin : origins) {
      if (origin.trim().length() != 0)
        for (String allowedOrigin : allowedOrigins) {
          if (allowedOrigin.contains("*")) {
            Matcher matcher = createMatcher(origin, allowedOrigin);
            if (matcher.matches())
              return true; 
            continue;
          } 
          if (allowedOrigin.equals(origin))
            return true; 
        }  
    } 
    return false;
  }
  
  private Matcher createMatcher(String origin, String allowedOrigin) {
    String regex = parseAllowedWildcardOriginToRegex(allowedOrigin);
    Pattern pattern = Pattern.compile(regex);
    return pattern.matcher(origin);
  }
  
  private String parseAllowedWildcardOriginToRegex(String allowedOrigin) {
    String regex = allowedOrigin.replace(".", "\\.");
    return regex.replace("*", ".*");
  }
  
  private boolean isSimpleRequest(HttpServletRequest request) {
    String method = request.getMethod();
    if (SIMPLE_HTTP_METHODS.contains(method))
      return (request.getHeader("Access-Control-Request-Method") == null); 
    return false;
  }
  
  private boolean isPreflightRequest(HttpServletRequest request) {
    String method = request.getMethod();
    if (!"OPTIONS".equalsIgnoreCase(method))
      return false; 
    if (request.getHeader("Access-Control-Request-Method") == null)
      return false; 
    return true;
  }
  
  private void handleSimpleResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
    response.setHeader("Access-Control-Allow-Origin", origin);
    if (!this.anyOriginAllowed)
      response.addHeader("Vary", "Origin"); 
    if (this.allowCredentials)
      response.setHeader("Access-Control-Allow-Credentials", "true"); 
    if (!this.exposedHeaders.isEmpty())
      response.setHeader("Access-Control-Expose-Headers", commify(this.exposedHeaders)); 
  }
  
  private void handlePreflightResponse(HttpServletRequest request, HttpServletResponse response, String origin) {
    boolean methodAllowed = isMethodAllowed(request);
    if (!methodAllowed)
      return; 
    List<String> headersRequested = getAccessControlRequestHeaders(request);
    boolean headersAllowed = areHeadersAllowed(headersRequested);
    if (!headersAllowed)
      return; 
    response.setHeader("Access-Control-Allow-Origin", origin);
    if (!this.anyOriginAllowed)
      response.addHeader("Vary", "Origin"); 
    if (this.allowCredentials)
      response.setHeader("Access-Control-Allow-Credentials", "true"); 
    if (this.preflightMaxAge > 0)
      response.setHeader("Access-Control-Max-Age", String.valueOf(this.preflightMaxAge)); 
    response.setHeader("Access-Control-Allow-Methods", commify(this.allowedMethods));
    if (this.anyHeadersAllowed) {
      response.setHeader("Access-Control-Allow-Headers", commify(headersRequested));
    } else {
      response.setHeader("Access-Control-Allow-Headers", commify(this.allowedHeaders));
    } 
  }
  
  private boolean isMethodAllowed(HttpServletRequest request) {
    String accessControlRequestMethod = request.getHeader("Access-Control-Request-Method");
    LOG.debug("{} is {}", "Access-Control-Request-Method", accessControlRequestMethod);
    boolean result = false;
    if (accessControlRequestMethod != null)
      result = this.allowedMethods.contains(accessControlRequestMethod); 
    LOG.debug("Method {} is" + (result ? "" : " not") + " among allowed methods {}", accessControlRequestMethod, this.allowedMethods);
    return result;
  }
  
  private List<String> getAccessControlRequestHeaders(HttpServletRequest request) {
    String accessControlRequestHeaders = request.getHeader("Access-Control-Request-Headers");
    LOG.debug("{} is {}", "Access-Control-Request-Headers", accessControlRequestHeaders);
    if (accessControlRequestHeaders == null)
      return Collections.emptyList(); 
    List<String> requestedHeaders = new ArrayList<>();
    String[] headers = StringUtil.csvSplit(accessControlRequestHeaders);
    for (String header : headers) {
      String h = header.trim();
      if (h.length() > 0)
        requestedHeaders.add(h); 
    } 
    return requestedHeaders;
  }
  
  private boolean areHeadersAllowed(List<String> requestedHeaders) {
    if (this.anyHeadersAllowed) {
      LOG.debug("Any header is allowed");
      return true;
    } 
    boolean result = true;
    for (String requestedHeader : requestedHeaders) {
      boolean headerAllowed = false;
      for (String allowedHeader : this.allowedHeaders) {
        if (requestedHeader.equalsIgnoreCase(allowedHeader.trim())) {
          headerAllowed = true;
          break;
        } 
      } 
      if (!headerAllowed) {
        result = false;
        break;
      } 
    } 
    LOG.debug("Headers [{}] are" + (result ? "" : " not") + " among allowed headers {}", requestedHeaders, this.allowedHeaders);
    return result;
  }
  
  private String commify(List<String> strings) {
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < strings.size(); i++) {
      if (i > 0)
        builder.append(","); 
      String string = strings.get(i);
      builder.append(string);
    } 
    return builder.toString();
  }
}

