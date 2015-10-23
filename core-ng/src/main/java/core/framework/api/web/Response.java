package core.framework.api.web;

import core.framework.api.http.ContentTypes;
import core.framework.api.http.HTTPHeaders;
import core.framework.api.http.HTTPStatus;
import core.framework.impl.web.response.BeanBody;
import core.framework.impl.web.response.ByteArrayBody;
import core.framework.impl.web.response.FileBody;
import core.framework.impl.web.response.TemplateBody;
import core.framework.impl.web.response.TextBody;

import java.io.File;

/**
 * @author neo
 */
public interface Response {
    static Response text(String text, HTTPStatus status, String contentType) {
        return new ResponseImpl(new TextBody(text))
            .contentType(contentType)
            .status(status);
    }

    static Response text(String text, String contentType) {
        return text(text, HTTPStatus.OK, contentType);
    }

    static Response bean(Object bean) {
        return bean(bean, HTTPStatus.OK);
    }

    static Response bean(Object bean, HTTPStatus status) {
        return new ResponseImpl(new BeanBody(bean))
            .contentType(ContentTypes.APPLICATION_JSON)
            .status(status);
    }

    static Response html(String templatePath, Object model) {
        return new ResponseImpl(new TemplateBody(templatePath, model))
            .status(HTTPStatus.OK)
            .contentType(ContentTypes.TEXT_HTML);
    }

    static Response empty() {
        return new ResponseImpl(new TextBody(""))
            .status(HTTPStatus.NO_CONTENT);
    }

    static Response bytes(byte[] bytes) {
        return bytes(bytes, ContentTypes.APPLICATION_OCTET_STREAM);
    }

    static Response bytes(byte[] bytes, String contentType) {
        return new ResponseImpl(new ByteArrayBody(bytes))
            .status(HTTPStatus.OK)
            .contentType(contentType);
    }

    static Response file(File file) {
        return new ResponseImpl(new FileBody(file))
            .status(HTTPStatus.OK);
    }

    static Response redirect(String url) {
        return new ResponseImpl(new TextBody(""))
            .header(HTTPHeaders.LOCATION, url)
            .status(HTTPStatus.SEE_OTHER);
    }

    static Response redirect(String url, HTTPStatus redirectStatus) {
        if (redirectStatus != HTTPStatus.SEE_OTHER
            && redirectStatus != HTTPStatus.MOVED_PERMANENTLY
            && redirectStatus != HTTPStatus.PERMANENT_REDIRECT
            && redirectStatus != HTTPStatus.TEMPORARY_REDIRECT)
            throw new Error("redirect status is not valid, status=" + redirectStatus);

        return new ResponseImpl(new TextBody(""))
            .header(HTTPHeaders.LOCATION, url)
            .status(redirectStatus);
    }

    HTTPStatus status();

    Response status(HTTPStatus status);

    Response header(String name, Object value);

    Response contentType(String contentType);

    Response cookie(CookieSpec spec, String value);
}
