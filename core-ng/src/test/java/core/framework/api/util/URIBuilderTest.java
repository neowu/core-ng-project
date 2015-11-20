package core.framework.api.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author neo
 */
public class URIBuilderTest {
    @Test
    public void encodePathSegment() {
        assertEquals("utf-8:%E2%9C%93", URIBuilder.encodePathSegment("utf-8:✓"));
        assertEquals("v1%20v2", URIBuilder.encodePathSegment("v1 v2"));
        assertEquals("v1+v2", URIBuilder.encodePathSegment("v1+v2"));
        assertEquals("v1%2Fv2", URIBuilder.encodePathSegment("v1/v2"));
    }

    @Test
    public void encodeQuery() {
        assertEquals("k1=v1", URIBuilder.encode(URIBuilder.QUERY_OR_FRAGMENT, "k1=v1"));
        assertEquals("k1=v1%20v2", URIBuilder.encode(URIBuilder.QUERY_OR_FRAGMENT, "k1=v1 v2"));
        assertEquals("k1=v1/v2?", URIBuilder.encode(URIBuilder.QUERY_OR_FRAGMENT, "k1=v1/v2?"));
    }

    @Test
    public void buildFullURL() {
        URIBuilder builder = new URIBuilder()
            .hostAddress("example.com")
            .addPath("path1")
            .addPath("path2");

        assertEquals("//example.com/path1/path2", builder.toURI());

        builder.addQueryParam("k1", "v1")
            .addQueryParam("k2", "v2");

        assertEquals("//example.com/path1/path2?k1=v1&k2=v2", builder.toURI());

        builder.scheme("http")
            .port(8080);
        assertEquals("http://example.com:8080/path1/path2?k1=v1&k2=v2", builder.toURI());
    }

    @Test
    public void buildFromExistingURI() {
        assertEquals("http://localhost/path%201/path2?k1=v1+v1&k2=v2", new URIBuilder("http://localhost/path%201/path2?k1=v1+v1&k2=v2").toURI());

        assertEquals("//localhost:8080/path1/path2/", new URIBuilder("//localhost:8080/path1").addPath("path2").addSlash().toURI());
    }

    @Test
    public void buildRelativeURL() {
        URIBuilder builder = new URIBuilder()
            .addPath("path1")
            .addPath("path2");

        assertEquals("path1/path2", builder.toURI());

        builder.addQueryParam("k1", "v1");
        builder.addQueryParam("k2", "v2");

        assertEquals("path1/path2?k1=v1&k2=v2", builder.toURI());

        builder.fragment("f1/f2");
        assertEquals("path1/path2?k1=v1&k2=v2#f1/f2", builder.toURI());
    }

    @Test
    public void buildRelativeURLWithTrailingSlash() {
        URIBuilder builder = new URIBuilder()
            .addSlash()
            .addPath("path1")
            .addPath("path2")
            .addSlash();

        assertEquals("/path1/path2/", builder.toURI());
    }
}