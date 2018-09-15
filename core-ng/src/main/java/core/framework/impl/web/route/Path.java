package core.framework.impl.web.route;

/**
 * @author neo
 */
public final class Path {
    public static Path parse(String path) {
        Path root = new Path("/");
        if ("/".equals(path)) return root;

        Path current = root;
        int length = path.length();
        int from = 1;

        for (int i = 1; i < length; i++) {
            char ch = path.charAt(i);
            if (ch == '/') {
                current.next = new Path(path.substring(from, i));
                current = current.next;
                from = i + 1;
            }
        }

        String lastPath = from == length ? "/" : path.substring(from);
        current.next = new Path(lastPath);

        return root;
    }

    public final String value;
    public Path next;

    private Path(String value) {
        this.value = value;
    }

    String subPath() {
        var builder = new StringBuilder();
        Path current = this;

        while (current != null) {
            if (builder.length() > 1 && !"/".equals(current.value)) builder.append('/');
            builder.append(current.value);
            current = current.next;
        }
        return builder.toString();
    }
}
