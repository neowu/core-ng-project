package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.impl.template.expression.CallTypeStack;
import core.framework.impl.template.fragment.CompositeFragment;
import core.framework.impl.template.fragment.ExpressionFragment;
import core.framework.impl.template.fragment.ForFragment;
import core.framework.impl.template.fragment.IfFragment;
import core.framework.impl.template.fragment.StaticFragment;
import core.framework.impl.template.location.TemplateLocation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class TemplateBuilder {
    private static final Pattern INCLUDE_STATEMENT_PATTERN = Pattern.compile("include ([/a-zA-Z1-9\\-_\\.]+)");

    private final TemplateLocation location;
    private final CallTypeStack stack;
    private final Deque<CompositeFragment> fragmentStack = new ArrayDeque<>();
    private StringBuilder currentContent = new StringBuilder();

    public TemplateBuilder(TemplateLocation location, Class<?> modelClass) {
        new ModelClassValidator(modelClass).validate();
        this.location = location;
        this.stack = new CallTypeStack(modelClass);
    }

    public Template build() {
        fragmentStack.add(new Template(stack.rootClass));
        parse(location);

        if (fragmentStack.size() != 1) {
            throw Exceptions.error("directive is not closed, handlers={}, location={}", fragmentStack.peek(), location);
        }

        return (Template) fragmentStack.remove();
    }

    public void parse(TemplateLocation location) {
        int lineNumber = 0;
        try (BufferedReader reader = location.reader()) {
            while (true) {
                String line = reader.readLine();
                if (line == null) break;

                lineNumber++;
                String locationInfo = location + ":" + lineNumber;

                if (isDirective(line, locationInfo)) {
                    processDirective(line, location, locationInfo);
                } else if (containsExpression(line)) {
                    processExpression(line, locationInfo);
                } else {
                    currentContent.append(line).append('\n');
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        addStaticContent();
    }

    private void processDirective(String line, TemplateLocation location, String locationInfo) {
        addStaticContent();
        int index = line.indexOf("<!--%");
        int endIndex = line.indexOf("%-->");
        String statement = line.substring(index + 5, endIndex).trim();
        if ("end".equals(statement)) {
            if (fragmentStack.size() <= 1) {
                throw Exceptions.error("unmatched end, location={}", locationInfo);
            }
            CompositeFragment handler = fragmentStack.pop();
            fragmentStack.peek().handlers.add(handler);
            if (handler instanceof ForFragment) {
                stack.paramClasses.remove(((ForFragment) handler).variable);
            }
        } else if (statement.startsWith("if")) {
            fragmentStack.push(new IfFragment(statement, stack, locationInfo));
        } else if (statement.startsWith("for")) {
            ForFragment fragment = new ForFragment(statement, stack, locationInfo);
            fragmentStack.push(fragment);
            stack.paramClasses.put(fragment.variable, fragment.valueClass);
        } else if (statement.startsWith("include")) {
            processInclude(statement, location, locationInfo);
        } else {
            throw Exceptions.error("unsupported directive, line={}, location={}", line, this.location);
        }
    }

    private void processInclude(String statement, TemplateLocation location, String locationInfo) {
        Matcher matcher = INCLUDE_STATEMENT_PATTERN.matcher(statement);
        if (!matcher.matches())
            throw Exceptions.error("include must match \"include path\", statement={}, location={}", statement, locationInfo);

        String includePath = matcher.group(1);

        int fragmentCountBeforeInclude = fragmentStack.size();

        TemplateLocation includeLocation = location.location(includePath);
        parse(includeLocation);

        if (fragmentStack.size() != fragmentCountBeforeInclude) {
            throw Exceptions.error("directives in include do not matches, location={}", statement, includeLocation);
        }
    }

    private void processExpression(String line, String locationInfo) {
        boolean inExpression = false;
        StringBuilder currentExpression = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '$' && i + 1 < line.length() && line.charAt(i + 1) == '{') {
                addStaticContent();
                inExpression = true;
                i++;
            } else if (ch == '}') {
                String expression = currentExpression.toString();
                fragmentStack.peek().handlers.add(new ExpressionFragment(expression, stack, locationInfo));
                currentExpression = new StringBuilder();
                inExpression = false;
            } else if (inExpression) {
                currentExpression.append(ch);
            } else {
                currentContent.append(ch);
            }
        }
        if (currentExpression.length() > 0)
            throw Exceptions.error("expression is not closed, expression={}, location={}", currentExpression, locationInfo);

        currentContent.append('\n');
    }

    private void addStaticContent() {
        if (currentContent.length() > 0) {
            fragmentStack.peek().handlers.add(new StaticFragment(currentContent.toString()));
            currentContent = new StringBuilder();
        }
    }

    private boolean isDirective(String line, String location) {
        int length = line.length();
        for (int i = 0; i < length; i++) {
            char ch = line.charAt(i);
            if (ch != ' ') {
                boolean match = line.startsWith("<!--%", i);
                if (match && !line.endsWith("%-->"))
                    throw Exceptions.error("directive must ends with \"%-->\", line={}, location={}", line, location);
                return match;
            }
        }
        return false;
    }

    private boolean containsExpression(String line) {
        return line.contains("${");
    }
}
