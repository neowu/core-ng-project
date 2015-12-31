package core.framework.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class URIBuilderTest {
    @Test
    public void encodePathSegment() {
        assertEquals("encode utf-8", "%E2%9C%93", URIBuilder.encodePathSegment("✓"));
        assertEquals("a%20b", URIBuilder.encodePathSegment("a b"));
        assertEquals("a%2Bb", URIBuilder.encodePathSegment("a+b")); // per RFC + should not be encoded by path segment, but we have to do it to keep compatible with other wrong impl, e.g. undertow and AWS S3
        assertEquals("a=b", URIBuilder.encodePathSegment("a=b"));
        assertEquals("a%3Fb", URIBuilder.encodePathSegment("a?b"));
        assertEquals("a%2Fb", URIBuilder.encodePathSegment("a/b"));
        assertEquals("a&b", URIBuilder.encodePathSegment("a&b"));
    }

    @Test
    public void encodeQueryParam() {
        assertEquals("a%20b", URIBuilder.encodeQueryParam("a b"));
        assertEquals("a%2Bb", URIBuilder.encodeQueryParam("a+b"));
        assertEquals("a%3Db", URIBuilder.encodeQueryParam("a=b"));
        assertEquals("a?b", URIBuilder.encodeQueryParam("a?b"));
        assertEquals("a/b", URIBuilder.encodeQueryParam("a/b"));
        assertEquals("a%26b", URIBuilder.encodeQueryParam("a&b"));
    }

    @Test
    public void encodeFragment() {
        assertEquals("a%20b", URIBuilder.encodeFragment("a b"));
        assertEquals("a+b", URIBuilder.encodeFragment("a+b"));
        assertEquals("a=b", URIBuilder.encodeFragment("a=b"));
        assertEquals("a?b", URIBuilder.encodeFragment("a?b"));
        assertEquals("a/b", URIBuilder.encodeFragment("a/b"));
        assertEquals("a&b", URIBuilder.encodeFragment("a&b"));
    }

    @Test
    public void buildFullURI() {
        assertEquals("http://localhost/path1/path2?k1=v1+v1&k2=v2", new URIBuilder("http://localhost/path1/path2?k1=v1+v1&k2=v2").toURI());

        assertEquals("//localhost:8080/path1/path2/", new URIBuilder("//localhost:8080/path1").addPath("path2").addPath("").toURI());
        assertEquals("//localhost:8080/path1/path2", new URIBuilder("//localhost:8080/path1/").addPath("path2").toURI());
        assertEquals("//localhost:8080/path1?k1=v1", new URIBuilder("//localhost:8080/path1").addQueryParam("k1", "v1").toURI());
    }

    @Test
    public void buildRelativeURI() {
        assertEquals("/?k1=v1%20v2", new URIBuilder("/").addQueryParam("k1", "v1 v2").toURI());
        assertEquals("path1/path2?k1=v1&k2=v2", new URIBuilder().addPath("path1").addPath("path2").addQueryParam("k1", "v1").addQueryParam("k2", "v2").toURI());
        assertEquals("path1/path2?k1=v1#f1", new URIBuilder().addPath("path1").addPath("path2").addQueryParam("k1", "v1").fragment("f1").toURI());
    }

    @Test
    public void buildRelativeURIWithTrailingSlash() {
        assertEquals("/path1/path2/", new URIBuilder("/").addPath("path1").addPath("path2").addPath("").toURI());
    }

    @Test
    public void buildRelativeURIWithExistingQuery() {
        assertEquals("/path1?k1=v1&k2=v2", new URIBuilder("/path1?k1=v1").addQueryParam("k2", "v2").toURI());
    }
}