package core.framework.impl.template.v2.html;

/**
 * @author neo
 */
public enum HTMLTokenType {
    TAG_START,      // <tag
    TAG_CLOSE,      // </tag>
    TAG_END,        // >
    TAG_END_CLOSE,  // />
    TEXT,
    ATTR_NAME,      // attr
    ATTR_VALUE,     // =value or ="value"
    COMMENT_START,  // <!--
    COMMENT_END,    // comment-->
    EOF
}
