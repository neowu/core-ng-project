package core.framework.impl.template;

import core.framework.api.util.Exceptions;
import core.framework.api.util.Lists;
import core.framework.impl.template.expression.CallTypeStack;

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
    private final CallTypeStack stack;
    private final List<FragmentHandler> handlers = Lists.newArrayList();
    private StringBuilder currentContent = new StringBuilder();
    private final Deque<CompositeHandler> blockHandlers = new ArrayDeque<>();
    private int currentLineNumber;

    public TemplateBuilder(String template, Class<?> modelClass) {
        this.template = template;
        this.stack = new CallTypeStack(modelClass);
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
            throw Exceptions.error("block is not closed, expression={}", blockHandlers.peek());
        }

        addStaticContentFragmentHandler();

        return new Template(handlers);
    }

    private void processDirective(String line) {
        addStaticContentFragmentHandler();
        int index = line.indexOf("<!--%");
        int endIndex = line.indexOf("%-->");
        String expression = line.substring(index + 5, endIndex).trim();
        if ("end".equals(expression)) {
            CompositeHandler handler = blockHandlers.pop();
            if (blockHandlers.isEmpty()) {
                handlers.add(handler);
            } else {
                blockHandlers.peek().add(handler);
            }
            if (handler instanceof ForHandler) {
                stack.paramClasses.remove(((ForHandler) handler).variable);
            }
        } else {
            if (expression.startsWith("if")) {
                blockHandlers.push(new IfHandler(expression, stack, line + ", line=" + currentLineNumber));
            } else if (expression.startsWith("for")) {
                ForHandler forHandler = new ForHandler(expression, stack, line + ", line=" + currentLineNumber);
                blockHandlers.push(forHandler);
                stack.paramClasses.put(forHandler.variable, forHandler.valueClass);
            } else {
                throw new Error("unsupported directive, line=" + line);
            }
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
                    handlers.add(new ExpressionHandler(expression.toString(), stack, line + ", line=" + currentLineNumber));
                } else {
                    blockHandlers.peek().add(new ExpressionHandler(expression.toString(), stack, line + ", line=" + currentLineNumber));
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
                blockHandlers.peek().add(new StaticContentHandler(currentContent.toString()));
            }
            currentContent = new StringBuilder();
        }
    }

    private boolean isDirective(String line) {
        return line.trim().startsWith("<!--%");
    }

    private boolean containsExpression(String line) {
        return line.contains("${");
    }
}
