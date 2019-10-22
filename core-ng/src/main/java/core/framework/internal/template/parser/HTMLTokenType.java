package core.framework.internal.template.parser;

/**
 * @author neo
 */
public enum HTMLTokenType {
    START_TAG,              // <tag
    START_TAG_END,          // >
    START_TAG_END_CLOSE,    // />
    END_TAG,        // </tag>
    TEXT,
    ATTRIBUTE_NAME,      // attr
    ATTRIBUTE_VALUE,     // =value or ="value"
    START_COMMENT,  // <!--
    END_COMMENT,    // comment-->
    EOF
}
