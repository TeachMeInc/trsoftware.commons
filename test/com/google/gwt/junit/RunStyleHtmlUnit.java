/*
 *  Copyright 2017 TR Software Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package com.google.gwt.junit;

import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptEngine;
import com.gargoylesoftware.htmlunit.javascript.JavaScriptErrorListener;
import com.gargoylesoftware.htmlunit.javascript.host.Window;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.dev.shell.HostedModePluginObject;
import com.google.gwt.thirdparty.guava.common.collect.ImmutableSet;
import net.sourceforge.htmlunit.corejs.javascript.ScriptableObject;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

//NOTE(alexe): This patch adds support for htmlunit-2.15 (instead of htmlunit-2.9, which is used by GWT 2.5.0 and included in gwt-dev.jar)
//NOTE(alexe): We don't want to use htmlunit-2.9 because it contains a memory leak that was fixed starting with htmlunit-2.10

//TODO(alexe): GWT now bundles HtmlUnit 2.13, so now that we've upgraded to GWT 2.8.2 (on 3/4/2018), can probably delete this class

/**
 * Launches a web-mode test via HTMLUnit.
 */
public class RunStyleHtmlUnit extends RunStyle {

  /**
   * Runs HTMLUnit in a separate thread.
   */
  protected static class HtmlUnitThread extends Thread implements AlertHandler,
      IncorrectnessListener, OnbeforeunloadHandler {

    private final BrowserVersion browser;
    private final boolean developmentMode;
    private final TreeLogger treeLogger;
    private final String url;
    private Object waitForUnload = new Object();

    public HtmlUnitThread(BrowserVersion browser, String url,
        TreeLogger treeLogger, boolean developmentMode) {
      this.browser = browser;
      this.url = url;
      this.treeLogger = treeLogger;
      this.setName("htmlUnit client thread");
      this.developmentMode = developmentMode;
    }

    public void handleAlert(Page page, String message) {
      treeLogger.log(TreeLogger.ERROR, "Alert: " + message);
    }

    public boolean handleEvent(Page page, String returnValue) {
      synchronized (waitForUnload) {
        waitForUnload.notifyAll();
      }
      return true;
    }

    public void notify(String message, Object origin) {
      if ("Obsolete content type encountered: 'text/javascript'.".equals(message)) {
        // silently eat warning about text/javascript MIME type
        return;
      }
      treeLogger.log(TreeLogger.WARN, message);
    }

    @Override
    public void run() {
      WebClient webClient = new WebClient(browser);
      webClient.setAlertHandler(this);
      // Adding a handler that ignores errors to work-around
      // https://sourceforge.net/tracker/?func=detail&aid=3090806&group_id=47038&atid=448266
      webClient.setCssErrorHandler(new ErrorHandler() {

        public void error(CSSParseException exception) {
          // ignore
        }

        public void fatalError(CSSParseException exception) {
          treeLogger.log(TreeLogger.WARN,
              "CSS fatal error: " + exception.getURI() + " ["
                  + exception.getLineNumber() + ":"
                  + exception.getColumnNumber() + "] " + exception.getMessage());
        }

        public void warning(CSSParseException exception) {
          // ignore
        }
      });
      webClient.setIncorrectnessListener(this);
      webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
      webClient.getOptions().setThrowExceptionOnScriptError(true);
      webClient.setOnbeforeunloadHandler(this);
      webClient.setJavaScriptErrorListener(new JavaScriptErrorListener() {

        @Override
        public void loadScriptError(HtmlPage htmlPage, URL scriptUrl,
            Exception exception) {
            treeLogger.log(TreeLogger.ERROR,
              "Load Script Error: " + exception, exception);
        }

        @Override
        public void malformedScriptURL(HtmlPage htmlPage, String url,
            MalformedURLException malformedURLException) {
          treeLogger.log(TreeLogger.ERROR,
              "Malformed Script URL: " + malformedURLException.getLocalizedMessage());
        }

        @Override
        public void scriptException(HtmlPage htmlPage,
            ScriptException scriptException) {
          treeLogger.log(TreeLogger.DEBUG,
              "Script Exception: " + scriptException.getLocalizedMessage() +
               ", line " + scriptException.getFailingLine());
        }

        @Override
        public void timeoutError(HtmlPage htmlPage, long allowedTime,
            long executionTime) {
          treeLogger.log(TreeLogger.ERROR,
              "Script Timeout Error " + executionTime + " > " + allowedTime);
        }
      });
      setupWebClient(webClient);
      try {
        Page page = webClient.getPage(url);
        webClient.waitForBackgroundJavaScriptStartingBefore(2000);
        if (treeLogger.isLoggable(TreeLogger.SPAM)) {
          treeLogger.log(TreeLogger.SPAM, "getPage returned "
              + ((HtmlPage) page).asXml());
        }
        // TODO(amitmanjhi): call webClient.closeAllWindows()
      } catch (FailingHttpStatusCodeException e) {
        treeLogger.log(TreeLogger.ERROR, "HTTP request failed", e);
        return;
      } catch (MalformedURLException e) {
        treeLogger.log(TreeLogger.ERROR, "Bad URL", e);
        return;
      } catch (IOException e) {
        treeLogger.log(TreeLogger.ERROR, "I/O error on HTTP request", e);
        return;
      }
    }

    protected void setupWebClient(WebClient webClient) {
      if (developmentMode) {
        JavaScriptEngine hostedEngine = new HostedJavaScriptEngine(webClient,
            treeLogger);
        webClient.setJavaScriptEngine(hostedEngine);
      }
    }
  }

  /**
   * JavaScriptEngine subclass that provides a hook of initializing the
   * __gwt_HostedModePlugin property on any new window, so it acts just like
   * Firefox with the XPCOM plugin installed.
   */
  private static class HostedJavaScriptEngine extends JavaScriptEngine {

    private static final long serialVersionUID = 3594816610842448691L;
    private final TreeLogger logger;

    public HostedJavaScriptEngine(WebClient webClient, TreeLogger logger) {
      super(webClient);
      this.logger = logger;
    }

    @Override
    public void initialize(WebWindow webWindow) {
      // Hook in the hosted-mode plugin after initializing the JS engine.
      super.initialize(webWindow);
      Window window = (Window) webWindow.getScriptObject();
      window.defineProperty("__gwt_HostedModePlugin",
          new HostedModePluginObject(this, logger), ScriptableObject.READONLY);
    }
  }

  private static final Map<String, BrowserVersion> BROWSER_MAP = createBrowserMap();

  /*
   * as long as this number is greater than 1, GWTTestCaseTest::testRetry will
   * pass
   */
  private static final int DEFAULT_TRIES = 1;

  private static final Set<Platform> PLATFORMS = ImmutableSet.of(Platform.HtmlUnitBug,
      Platform.HtmlUnitLayout, Platform.HtmlUnitUnknown);

  /**
   * Returns the list of browsers Htmlunit emulates as a comma separated string.
   */
  static String getBrowserList() {
    StringBuffer sb = new StringBuffer();
    for (String str : BROWSER_MAP.keySet()) {
      sb.append(str);
      sb.append(",");
    }
    if (sb.length() > 1) {
      return sb.substring(0, sb.length() - 1);
    }
    return sb.toString();
  }

  private static Map<String, BrowserVersion> createBrowserMap() {
    Map<String, BrowserVersion> browserMap = new HashMap<String, BrowserVersion>();
    for (BrowserVersion browser : new BrowserVersion[] {
        BrowserVersion.FIREFOX_17, BrowserVersion.FIREFOX_24, BrowserVersion.CHROME,
        BrowserVersion.INTERNET_EXPLORER_8, BrowserVersion.INTERNET_EXPLORER_9, BrowserVersion.INTERNET_EXPLORER_11}
        ) {
      browserMap.put(browser.getNickname(), browser);
    }
    return Collections.unmodifiableMap(browserMap);
  }

  private Set<BrowserVersion> browsers = new HashSet<BrowserVersion>();
  private boolean developmentMode;
  private final List<Thread> threads = new ArrayList<Thread>();

  /**
   * Create a RunStyle instance with the passed-in browser targets.
   */
  public RunStyleHtmlUnit(JUnitShell shell) {
    super(shell);
  }

  @Override
  public Set<Platform> getPlatforms() {
    return PLATFORMS;
  }

  @Override
  public int initialize(String args) {
    if (args == null || args.length() == 0) {
      // If no browsers specified, default to Firefox (GWT2.5 was using "FF3", but htmlunit-2.15 no longer has that version)
      args = BrowserVersion.FIREFOX_24.getNickname();
    }
    Set<BrowserVersion> browserSet = new HashSet<BrowserVersion>();
    for (String browserName : args.split(",")) {
      BrowserVersion browser = BROWSER_MAP.get(browserName);
      if (browser == null) {
        getLogger().log(
            TreeLogger.ERROR,
            "RunStyleHtmlUnit: Unknown browser " + "name " + browserName
                + ", expected browser name: one of " + BROWSER_MAP.keySet());
        return -1;
      }
      browserSet.add(browser);
    }
    browsers = Collections.unmodifiableSet(browserSet);

    setTries(DEFAULT_TRIES); // set to the default value for this RunStyle
    return browsers.size();
  }

  @Override
  public void launchModule(String moduleName) {
    for (BrowserVersion browser : browsers) {
      String url = shell.getModuleUrl(moduleName);
      HtmlUnitThread hut = createHtmlUnitThread(browser, url);
      TreeLogger logger = shell.getTopLogger();
      if (logger.isLoggable(TreeLogger.INFO)) {
        logger.log(TreeLogger.INFO,
            "Starting " + url + " on browser " + browser.getNickname());
      }
      /*
       * TODO (amitmanjhi): Is it worth pausing here and waiting for the main
       * test thread to get to an "okay" state.
       */
      hut.start();
      threads.add(hut);
    }
  }

  public int numBrowsers() {
    return browsers.size();
  }

  @Override
  public boolean setupMode(TreeLogger logger, boolean developmentMode) {
    this.developmentMode = developmentMode;
    return true;
  }

  protected HtmlUnitThread createHtmlUnitThread(BrowserVersion browser,
      String url) {
    return new HtmlUnitThread(browser, url, shell.getTopLogger().branch(
        TreeLogger.SPAM, "logging for HtmlUnit thread"), developmentMode);
  }
}
