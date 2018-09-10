package core.framework.web;

import core.framework.api.http.HTTPStatus;
import core.framework.http.ContentType;
import core.framework.http.HTTPHeaders;
import core.framework.impl.web.response.BeanBody;
import core.framework.impl.web.response.ByteArrayBody;
import core.framework.impl.web.response.FileBody;
import core.framework.impl.web.response.ResponseImpl;
import core.framework.impl.web.response.TemplateBody;
import core.framework.impl.web.response.TextBody;

import java.nio.file.Path;
import java.util.Optional;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public interface Response {
    static Response text(String text) {
        return new ResponseImpl(new TextBody(text))
                .contentType(ContentType.TEXT_PLAIN);
    }

    static Response bean(Object bean) {
        if (bean == null) throw new Error("bean must not be null");
        return new ResponseImpl(new BeanBody(bean))
                .contentType(ContentType.APPLICATION_JSON);
    }

    static Response html(String templatePath, Object model) {
        return html(templatePath, model, null);
    }

    static Response html(String templatePath, Object model, String language) {
        return new ResponseImpl(new TemplateBody(templatePath, model, language))
                .contentType(ContentType.TEXT_HTML);
    }

    static Response empty() {
        return new ResponseImpl(new ByteArrayBody(new byte[0]))
                .status(HTTPStatus.NO_CONTENT);
    }

    static Response bytes(byte[] bytes) {
        return new ResponseImpl(new ByteArrayBody(bytes))
                .contentType(ContentType.APPLICATION_OCTET_STREAM);
    }

    static Response file(Path path) {
        return new ResponseImpl(new FileBody(path));
    }

    static Response redirect(String url) {
        return redirect(url, HTTPStatus.SEE_OTHER);
    }

    static Response redirect(String url, HTTPStatus redirectStatus) {
        if (redirectStatus != HTTPStatus.SEE_OTHER
                && redirectStatus != HTTPStatus.MOVED_PERMANENTLY
                && redirectStatus != HTTPStatus.PERMANENT_REDIRECT
                && redirectStatus != HTTPStatus.TEMPORARY_REDIRECT)
            throw new Error(format("invalid redirect status, status={}", redirectStatus));

        return new ResponseImpl(new ByteArrayBody(new byte[0]))
                .header(HTTPHeaders.LOCATION, url)
                .status(redirectStatus);
    }

    HTTPStatus status();

    Response status(HTTPStatus status);

    Response header(String name, Object value);

    Optional<ContentType> contentType();

    Response contentType(ContentType contentType);

    Response cookie(CookieSpec spec, String value);
}
