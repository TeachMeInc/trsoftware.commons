/*
 * Copyright 2018 TR Software Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package solutions.trsoftware.commons.server.servlet;

import solutions.trsoftware.commons.server.servlet.config.InitParameters;
import solutions.trsoftware.commons.server.servlet.config.WebConfigException;
import solutions.trsoftware.commons.server.servlet.config.WebConfigParser;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static solutions.trsoftware.commons.shared.util.StringUtils.isBlank;

/**
 * Provides basic utility methods that can be helpful when writing a new {@link HttpServlet}
 *
 * @author Alex, 10/31/2017
 */
public abstract class BaseHttpServlet extends HttpServlet {

  protected static String getRequiredParameter(HttpServletRequest request, String paramName) throws IOException, RequestException {
    String value = request.getParameter(paramName);
    if (isBlank(value)) {
      throw new RequestException(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter " + paramName);  // Bad Request (missing required parameters)
    }
    return value;
  }

  /**
   * Sets the fields of the given {@link InitParameters} object from the {@code init-param} values present in the {@link ServletConfig}.
   * @return the same instance that was passed as the argument, after its fields have been populated.
   * @see WebConfigParser#parse(ServletConfig, InitParameters)
   */
  protected <P extends InitParameters> P parse(P parameters) throws ServletException {
    ServletConfig servletConfig = getServletConfig();
    try {
      P parsedParams = WebConfigParser.parse(servletConfig, parameters);
      servletConfig.getServletContext().log(
          String.format("Servlet config (servlet-name: \"%s\", servlet-class: %s) parsed from init-param values: %s",
              servletConfig.getServletName(), getClass().getSimpleName(), parsedParams));
      return parsedParams;
    }
    catch (WebConfigException e) {
      throw new ServletException(String.format("Invalid configuration for servlet '%s' (%s): %s",
          servletConfig.getServletName(), getClass(), e.getMessage()), e);
    }
  }
  
}
