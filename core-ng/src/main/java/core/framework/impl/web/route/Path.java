package core.framework.impl.web.route;

/**
 * @author neo
 */
public final class Path {
    public static Path parse(String path) {
        Path root = new Path("/");

        if ("/".equals(path)) return root;

        Path current = root;
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i < path.length(); i++) {
            char ch = path.charAt(i);
            if (ch != '/') {
                builder.append(ch);
            } else {
                current.next = new Path(builder.toString());
                current = current.next;
                builder = new StringBuilder();
            }
        }

        String lastPath = builder.length() == 0 ? "/" : builder.toString();
        current.next = new Path(lastPath);

        return root;
    }

    public final String value;
    public Path next;

    private Path(String value) {
        this.value = value;
    }

    public String subPath() {
        StringBuilder builder = new StringBuilder();
        Path current = this;

        while (current != null) {
            if (builder.length() > 1 && !"/".equals(current.value)) builder.append('/');
            builder.append(current.value);
            current = current.next;
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return value;
    }
}
