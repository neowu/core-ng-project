package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * @author neo
 */
public class TemplateBuilder {
    private final String template;
    private final List<FragmentHandler> handlers = Lists.newArrayList();
    private StringBuilder currentContent = new StringBuilder();
    private Deque<BlockHandler> blockHandlers = new ArrayDeque<>();
    private int currentLineNumber;

    public TemplateBuilder(String template) {
        this.template = template;
    }

    public Template build() {
        try (BufferedReader reader = new BufferedReader(new StringReader(template))) {
            while (true) {
                String line = reader.readLine();
                currentLineNumber++;
                if (line == null) break;
                if (isDirective(line)) {
                    processDirective(line);
                } else if (containsExpression(line)) {
                    processExpression(line);
                } else {
                    currentContent.append(line).append('\n');
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (!blockHandlers.isEmpty()) {
            throw Exceptions.error("block is not closed, expression={}", blockHandlers.peek().expression);
        }

        addStaticContentFragmentHandler();

        return new Template(handlers);
    }

    private void processDirective(String line) {
        addStaticContentFragmentHandler();
        int index = line.indexOf("<!--[");
        int endIndex = line.indexOf("]-->");
        String expression = line.substring(index + 5, endIndex);
        if (expression.startsWith("/")) {
            BlockHandler handler = blockHandlers.pop();
            expression = expression.substring(1);
            if (!handler.expression.equals(expression))
                throw Exceptions.error("expression block does not match, lineNumber={}, expectedExpression={}", currentLineNumber, handler.expression);
            if (blockHandlers.isEmpty()) {
                handlers.add(handler);
            } else {
                blockHandlers.peek().handlers.add(handler);
            }
        } else {
            blockHandlers.push(new BlockHandler(expression));
        }
    }

    private void processExpression(String line) {
        boolean inExpression = false;
        StringBuilder expression = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '$' && i + 1 < line.length() && line.charAt(i + 1) == '{') {
                addStaticContentFragmentHandler();
                inExpression = true;
                i++;
            } else if (ch == '}') {
                if (blockHandlers.isEmpty()) {
                    handlers.add(new ExpressionHandler(expression.toString()));
                } else {
                    blockHandlers.peek().handlers.add(new ExpressionHandler(expression.toString()));
                }
                expression = new StringBuilder();
                inExpression = false;
            } else if (inExpression) {
                expression.append(ch);
            } else {
                currentContent.append(ch);
            }
        }
        if (expression.length() > 0)
            throw Exceptions.error("expression is not closed, lineNumber={}, expression={}", currentLineNumber, expression.toString());

        currentContent.append('\n');
    }

    private void addStaticContentFragmentHandler() {
        if (currentContent.length() > 0) {
            if (blockHandlers.isEmpty()) {
                handlers.add(new StaticContentHandler(currentContent.toString()));
            } else {
                blockHandlers.peek().handlers.add(new StaticContentHandler(currentContent.toString()));
            }
            currentContent = new StringBuilder();
        }
    }

    private boolean isDirective(String line) {
        return line.trim().startsWith("<!--[");
    }

    private boolean containsExpression(String line) {
        return line.contains("${");
    }
}
