package core.framework.module;

import core.framework.util.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author neo
 */
public class AllowCIDRParser {
    private final String value;

    public AllowCIDRParser(String value) {
        this.value = value;
    }

    // support two formats,
    // 1. cidr,cidr
    // 2. name1: cidr, cidr; name2: cidr
    public String[] parse() {
        if (Strings.isBlank(value)) return new String[0];
        if (value.indexOf(':') > 0) {
            return parseSemicolonDelimited(value);
        } else {
            return parseCommaDelimited(value);
        }
    }

    private String[] parseCommaDelimited(String value) {
        return Arrays.stream(Strings.split(value, ',')).map(String::strip).toArray(String[]::new);
    }

    private String[] parseSemicolonDelimited(String value) {
        List<String> results = new ArrayList<>();
        for (String item : Strings.split(value, ';')) {
            if (Strings.isBlank(item)) continue;
            int index = item.indexOf(':');
            Arrays.stream(Strings.split(item.substring(index + 1), ','))
                  .map(String::strip)
                  .forEach(results::add);
        }
        return results.toArray(String[]::new);
    }
}
