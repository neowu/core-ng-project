package core.framework.impl.template.fragment;

import core.framework.api.log.Markers;
import core.framework.impl.template.TemplateContext;
import core.framework.impl.template.TemplateMetaContext;
import core.framework.impl.template.expression.ExpressionBuilder;
import core.framework.impl.template.expression.ExpressionHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * @author neo
 */
public class URLFragment implements Fragment {  // this is for dynamic href/src only, static href won't be processed during compilation
    /*
       refer to http://www.ietf.org/rfc/rfc3986.txt
       pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
       pct-encoded   = "%" HEXDIG HEXDIG
       unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
       sub-delims    = "!" / "$" / "&" / "'" / "(" / ")" / "*" / "+" / "," / ";" / "="
       gen-delims    = ":" / "/" / "?" / "#" / "[" / "]" / "@"
       segment       = *pchar
       query         = *( pchar / "/" / "?" )
       fragment      = *( pchar / "/" / "?" )
    */
    private static final BitSet VALID_URI = new BitSet(128);

    static {
        // unreserved
        for (int i = 'a'; i <= 'z'; i++) {
            VALID_URI.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            VALID_URI.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            VALID_URI.set(i);
        }
        VALID_URI.set('-');
        VALID_URI.set('.');
        VALID_URI.set('_');
        VALID_URI.set('~');
        // sub-delims
        VALID_URI.set('!');
        VALID_URI.set('$');
        VALID_URI.set('&');
        VALID_URI.set('\'');
        VALID_URI.set('(');
        VALID_URI.set(')');
        VALID_URI.set('*');
        VALID_URI.set('+');
        VALID_URI.set(',');
        VALID_URI.set(';');
        VALID_URI.set('=');
        // ":" and "@"
        VALID_URI.set(':');
        VALID_URI.set('@');
        // fragment and query
        VALID_URI.set('/');
        VALID_URI.set('?');
        // gen-delims
        VALID_URI.set(':');
        VALID_URI.set('/');
        VALID_URI.set('?');
        VALID_URI.set('#');
        VALID_URI.set('[');
        VALID_URI.set(']');
        VALID_URI.set('@');
        // escape char
        VALID_URI.set('%');
    }

    static boolean isValidURL(String url) {
        int length = url.length();
        if (length == 0) return false;
        if (url.contains("javascript:")) return false;
        for (int i = 0; i < length; i++) {
            char ch = url.charAt(i);
            if (!VALID_URI.get(ch)) return false;
        }
        return true;
    }

    private final Logger logger = LoggerFactory.getLogger(URLFragment.class);
    private final ExpressionHolder expression;
    private final String location;
    private final boolean cdn;

    public URLFragment(String expression, TemplateMetaContext context, boolean cdn, String location) {
        this.expression = new ExpressionBuilder(expression, context, location).build();
        this.cdn = cdn;
        this.location = location;
    }

    @Override
    public void process(StringBuilder builder, TemplateContext context) {
        String url = String.valueOf(expression.eval(context));
        builder.append(url(url, context));
    }

    private String url(String url, TemplateContext context) {
        if (!isValidURL(url)) {
            logger.warn(Markers.errorCode("ILLEGAL_URL"), "illegal url detected, url={}, location={}", url, location);
            return "";
        }
        return cdn ? context.cdn.url(url) : url;
    }
}
