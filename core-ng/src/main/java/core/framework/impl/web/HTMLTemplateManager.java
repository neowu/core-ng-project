package core.framework.impl.web;

import core.framework.api.util.Maps;
import core.framework.api.util.StopWatch;
import core.framework.impl.template.Template;
import core.framework.impl.template.TemplateBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * @author neo
 */
public class HTMLTemplateManager {
    private final Logger logger = LoggerFactory.getLogger(HTMLTemplateManager.class);
    private final Map<String, Template> templates = Maps.newConcurrentHashMap();
    private final Path webPath;
    private boolean refreshTemplateIfModified;

    public HTMLTemplateManager() {
        webPath = locateWebPath();
    }

    private Path locateWebPath() {
        String webPathValue = System.getProperty("core.web");
        if (webPathValue != null) {
            Path webPath = Paths.get(webPathValue).toAbsolutePath();
            if (Files.exists(webPath) && Files.isDirectory(webPath)) {
                logger.info("found -Dcore.web, use it as web path, path={}", webPath);
                return webPath;
            }
        } else {
            Path webPath = Paths.get("./src/main/dist/web").toAbsolutePath();
            if (Files.exists(webPath) && Files.isDirectory(webPath)) {
                logger.warn("found local web path, this should only happen in local dev env, path={}", webPath);
                refreshTemplateIfModified = true;
                return webPath;
            }
        }
        logger.info("can not locate web path");
        return null;
    }

    public String process(String templateName, Object model) {
        StopWatch watch = new StopWatch();
        try {
            Template template = templates.computeIfAbsent(templateKey(templateName), (key) -> loadTemplate(templateName, model.getClass()));
            return template.process(model, null);
        } finally {
            logger.debug("process, templateName={}, elapsedTime={}", templateName, watch.elapsedTime());
        }
    }

    public void addTemplate(String templateName, Class<?> modelClass) {
        StopWatch watch = new StopWatch();
        try {
            templates.put(templateKey(templateName), loadTemplate(templateName, modelClass));
        } finally {
            logger.info("add template, templateName={}, modelClass={}, elapsedTime={}", templateName, modelClass.getCanonicalName(), watch.elapsedTime());
        }
    }

    private Template loadTemplate(String templateName, Class<?> modelClass) {
        Path templatePath = webPath.resolve(templateName).toAbsolutePath();
        logger.debug("load template, path={}", templatePath);
        String template = core.framework.api.util.Files.text(templatePath);
        return new TemplateBuilder(template, modelClass).build();
    }

    private String templateKey(String templateName) {
        if (webPath == null)
            throw new Error("web path does not exist, check -Dcore.web or set working dir to be module path for local dev env.");

        if (refreshTemplateIfModified) {
            try {
                return templateName + ":" + Files.getLastModifiedTime(webPath.resolve(templateName)).toMillis();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } else {
            return templateName;
        }
    }
}
