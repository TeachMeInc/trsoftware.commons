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

import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.WebClient;
import junit.framework.TestCase;
import solutions.trsoftware.commons.server.servlet.testutil.DummyHttpServletRequest;
import solutions.trsoftware.commons.server.servlet.testutil.LiveServletTestCase;
import solutions.trsoftware.commons.server.testutil.EmbeddedTomcatServer;
import solutions.trsoftware.commons.server.testutil.PerformanceComparison;
import solutions.trsoftware.commons.shared.annotations.Slow;
import solutions.trsoftware.commons.shared.testutil.AssertUtils;
import solutions.trsoftware.commons.shared.testutil.TestData;
import solutions.trsoftware.commons.shared.util.MapUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static solutions.trsoftware.commons.server.servlet.ServletUtils.*;
import static solutions.trsoftware.commons.shared.testutil.AssertUtils.assertSameSequence;
import static solutions.trsoftware.commons.shared.util.RandomUtils.nextIntInRange;
import static solutions.trsoftware.commons.shared.util.RandomUtils.rnd;

public class ServletUtilsTest extends TestCase {

  public void testRequestParametersAsSortedStringMap() throws Exception {
    {
      Map<String, String[]> goodMap = new HashMap<>();
      goodMap.put("foo", new String[]{"a"});
      goodMap.put("bar", new String[]{"b"});
      assertEquals(
          MapUtils.stringMap("foo", "a", "bar", "b"),
          getRequestParametersAsSortedStringMap(goodMap));
    }
    {
      final Map<String, String[]> badMap = new HashMap<>();
      badMap.put("foo", new String[]{"a"});
      badMap.put("bar", new String[]{"b", "c"});
      AssertUtils.assertThrows(IllegalArgumentException.class, (Runnable)() -> getRequestParametersAsSortedStringMap(badMap));
    }
  }

  public void testExtractFirstPathElement() throws Exception {
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/gameserv")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/gameserv/")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/bar/gameserv")));
    assertEquals("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo/bar/gameserv/")));

    assertEquals("bar", extractFirstPathElement(new DummyHttpServletRequest("foo/bar/gameserv/")));

    // the following URIs don't have a first path element
    assertNull("foo", extractFirstPathElement(new DummyHttpServletRequest("/")));
    assertNull("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo")));
    assertNull("foo", extractFirstPathElement(new DummyHttpServletRequest("/foo.html")));
  }

  public void testExtractAllPathElements() throws Exception {
    assertSameSequence(asEnumeration(), extractAllPathElements(new DummyHttpServletRequest("/")));
    assertSameSequence(asEnumeration(), extractAllPathElements(new DummyHttpServletRequest("")));
    AssertUtils.assertThrows(NullPointerException.class,
        (Runnable)() -> extractAllPathElements(new DummyHttpServletRequest((String)null)));
    assertSameSequence(asEnumeration("foo"), extractAllPathElements(new DummyHttpServletRequest("/foo/")));
    assertSameSequence(asEnumeration("foo"), extractAllPathElements(new DummyHttpServletRequest("/foo/")));
    assertSameSequence(asEnumeration("foo", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/gameserv")));
    assertSameSequence(asEnumeration("foo", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/bar/gameserv")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/bar/gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("/foo/bar//gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("//foo/bar//gameserv/")));
    assertSameSequence(asEnumeration("foo", "bar", "gameserv"), extractAllPathElements(new DummyHttpServletRequest("//foo/bar//gameserv//")));
  }

  public static Enumeration asEnumeration(final Object... items) {
    return new Enumeration() {
      int i = 0;

      public boolean hasMoreElements() {
        return i < items.length;
      }

      public Object nextElement() {
        return items[i++];
      }
    };
  }

  public void testReplaceQueryStringParameter() throws Exception {
    for (String protocol : new String[]{"http", "https"}) {
      DummyHttpServletRequest dummyRequest = new DummyHttpServletRequest(protocol + "://example.com/page", "foo=bar&baz=1");
      assertEquals(protocol + "://example.com/page?bar=foo&baz=1",
          replaceQueryStringParameter(dummyRequest, "foo", "bar", "bar", "foo"));
      assertEquals(protocol + "://example.com/page?baz=2&baz=1",
          replaceQueryStringParameter(dummyRequest, "foo", "bar", "baz", "2"));
      assertEquals(protocol + "://example.com/page?foo=bar&baz=2",
          replaceQueryStringParameter(dummyRequest, "baz", "1", "baz", "2"));
      assertEquals(protocol + "://example.com/page?foo=bar&baz=1",
          replaceQueryStringParameter(dummyRequest, "baz", "2", "baz", "3"));  // param/value combination not present, so url returned unmodified
      assertEquals(protocol + "://example.com/page?foo=bar&baz=1",
          replaceQueryStringParameter(dummyRequest, "baz", "bar", "baz", "2")); // param/value combination not present, so url returned unmodified
    }
  }

  public void testAddIndexedMultivaluedParams() throws Exception {
    Map<String, String> map = MapUtils.sortedMap("foo", "bar");
    assertSame(map, addIndexedMultivaluedParams(map, "x", "a", 25, "foo"));
    // should have added 3 new entries (("x0", "a"), ("x1", 25), and ("x2", "foo"))
    // to the given map
    assertEquals(4, map.size());
    assertEquals("a", map.get("x0"));
    assertEquals("25", map.get("x1"));
    assertEquals("foo", map.get("x2"));
  }

  public void testReadIndexedMultivaluedParams() throws Exception {
    assertEquals(
        Arrays.asList("a", "25", "foo"),
        readIndexedMultivaluedParams(
            new DummyHttpServletRequest(MapUtils.hashMap("foo", "bar", "x2", "foo", "x0", "a", "x1", "25")),
            "x"));
    assertEquals(
        Arrays.<String>asList(),  // missing "x0", so returns nothing
        readIndexedMultivaluedParams(
            new DummyHttpServletRequest(MapUtils.hashMap("foo", "bar", "x2", "foo", "x1", "25")),
            "x"));
    assertEquals(
        Arrays.<String>asList("a"),  // missing "x1", so returns just the value for x0
        readIndexedMultivaluedParams(
            new DummyHttpServletRequest(MapUtils.hashMap("foo", "bar", "x2", "foo", "x0", "a")),
            "x"));
  }

  public void testGetBaseUrl() throws Exception {
    for (String protocol : new String[]{"http", "https"}) {
      assertEquals(protocol + "://localhost:8088", getBaseURL(
          new DummyHttpServletRequest().setUrl(protocol + "://localhost:8088/errorPage").setUri("/errorPage")).toString());
      assertEquals(protocol + "://localhost:8088", getBaseURL(
          new DummyHttpServletRequest().setUrl(protocol + "://localhost:8088/").setUri("/")).toString());
      assertEquals(protocol + "://example.com", getBaseURL(
          new DummyHttpServletRequest().setUrl(protocol + "://example.com/foo/bar").setUri("/")).toString());
    }
  }

  public void testGetRequestURL() throws Exception {
    for (int i = 0; i < 100; i++) {
      for (String protocol : new String[]{"http", "https"}) {
        String url = TestData.randomURL(protocol, nextIntInRange(2, 4), rnd.nextBoolean(),
            nextIntInRange(1, 5), nextIntInRange(3, 6));
        // 1) try without caching
        {
          DummyHttpServletRequest request = new DummyHttpServletRequest().setUrl(url);
          URL parsedURL = ServletUtils.getRequestURL(request, false);
          assertEquals(url, parsedURL.toString());
          assertNull(request.getAttribute(ServletUtils.PARSED_URL_ATTR));
          // we expect a new URL instance returned each time
          assertNotSame(parsedURL, ServletUtils.getRequestURL(request, false));
        }
        // 2) try with caching
        {
          DummyHttpServletRequest request = new DummyHttpServletRequest().setUrl(url);
          URL parsedURL = ServletUtils.getRequestURL(request, true);
          assertEquals(url, parsedURL.toString());
          assertSame(parsedURL, request.getAttribute(ServletUtils.PARSED_URL_ATTR));
          // we expect the same URL instance returned each time
          assertSame(parsedURL, ServletUtils.getRequestURL(request, true));
          // we expect the 1-arg version of the method to do the same
          assertSame(parsedURL, ServletUtils.getRequestURL(request));
          // however, if cache=false, we expect the cached instance not to be used
          assertNotSame(parsedURL, ServletUtils.getRequestURL(request, false));
        }
      }
    }
  }

  /**
   * Compares the performance of {@link ServletUtils#getRequestURL(HttpServletRequest, boolean)} with and without
   * caching.
   * @see GetRequestURLBenchmarkServlet
   */
  @Slow
  public void testGetRequestURLBenchmark() throws Exception {
    LinkedHashMap<Integer, Double> results = new LinkedHashMap<>();
    try (EmbeddedTomcatServer embeddedTomcat = new EmbeddedTomcatServer();
             WebClient webClient = new WebClient()) {
      String urlPattern = TestData.randomURI(10);
      EmbeddedTomcatServer.ServletHandle servletHandle = embeddedTomcat.addServlet(
          embeddedTomcat.addContext(TestData.randomURI(5)),
          GetRequestURLBenchmarkServlet.class.getSimpleName(),
          new GetRequestURLBenchmarkServlet(), urlPattern);
      embeddedTomcat.start();
      for (int nReads: new int[]{1, 2, 3, 5, 8, 13, 21, 34, 55, 89}) {  // fibonacci sequence
        TextPage page = webClient.getPage(servletHandle.getUrlBuilder()
            .append('?').append("nReads").append('=').append(nReads).toString());
        results.put(nReads, Double.parseDouble(LiveServletTestCase.printResponse(page)));
      }
    }
    System.out.println("================================================================================");
    System.out.println("Results (nReads -> multiplier [cached vs. uncached]");
    System.out.println("================================================================================");
    for (Map.Entry<Integer, Double> entry : results.entrySet()) {
      System.out.printf("%d -> %.2f%n", entry.getKey(), entry.getValue());
    }
  }


  private static class GetRequestURLBenchmarkServlet extends BaseHttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
      Integer nReads = getRequiredParameter(req, "nReads", Integer::parseInt);
      double multiplier = PerformanceComparison.compare(
          new PerformanceComparison.NamedRunnable("getRequestURL(cache=true)") {
            @Override
            public void run() {
              ServletUtils.getRequestURL(req, true);
            }
          },
          new PerformanceComparison.NamedRunnable("getRequestURL(cache=false)") {
            @Override
            public void run() {
              ServletUtils.getRequestURL(req, false);
            }
          },
          nReads
      );
      resp.getWriter().print(multiplier);
    }
  }

}