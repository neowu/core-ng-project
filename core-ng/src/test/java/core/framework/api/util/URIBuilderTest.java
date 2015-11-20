package core.framework.api.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author neo
 */
public class URIBuilderTest {
    @Test
    public void encodePathSegment() {
        Assert.assertEquals("utf-8:%E2%9C%93", URIBuilder.encodePathSegment("utf-8:✓"));
        Assert.assertEquals("v1%20v2", URIBuilder.encodePathSegment("v1 v2"));
        Assert.assertEquals("v1+v2", URIBuilder.encodePathSegment("v1+v2"));
        Assert.assertEquals("v1%2Fv2", URIBuilder.encodePathSegment("v1/v2"));
    }

    @Test
    public void encodeQuery() {
        Assert.assertEquals("k1=v1", URIBuilder.encode(URIBuilder.QUERY, "k1=v1"));
        Assert.assertEquals("k1=v1%20v2", URIBuilder.encode(URIBuilder.QUERY, "k1=v1 v2"));
        Assert.assertEquals("k1=v1/v2?", URIBuilder.encode(URIBuilder.QUERY, "k1=v1/v2?"));
    }

    @Test
    public void buildFullURL() {
        URIBuilder builder = new URIBuilder()
            .hostAddress("example.com")
            .addPath("path1")
            .addPath("path2");

        Assert.assertEquals("//example.com/path1/path2", builder.toURI());

        builder.addQueryParam("k1", "v1")
            .addQueryParam("k2", "v2");

        Assert.assertEquals("//example.com/path1/path2?k1=v1&k2=v2", builder.toURI());

        builder.scheme("http")
            .port(8080);
        Assert.assertEquals("http://example.com:8080/path1/path2?k1=v1&k2=v2", builder.toURI());
    }

    @Test
    public void buildFromExistingURI() {
        URIBuilder builder = new URIBuilder("http://localhost/path%201/path2?k1=v1+v1&k2=v2");
        Assert.assertEquals("http://localhost/path%201/path2?k1=v1+v1&k2=v2", builder.toURI());
    }

    @Test
    public void buildRelativeURL() {
        URIBuilder builder = new URIBuilder()
            .addPath("path1")
            .addPath("path2");

        Assert.assertEquals("path1/path2", builder.toURI());

        builder.addQueryParam("k1", "v1");
        builder.addQueryParam("k2", "v2");

        Assert.assertEquals("path1/path2?k1=v1&k2=v2", builder.toURI());
    }

    @Test
    public void buildRelativeURLWithTrailingSlash() {
        URIBuilder builder = new URIBuilder()
            .addSlash()
            .addPath("path1")
            .addPath("path2")
            .addSlash();

        Assert.assertEquals("/path1/path2/", builder.toURI());
    }
}