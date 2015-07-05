package core.framework.test.db;

import core.framework.api.db.Database;
import core.framework.api.db.Query;
import core.framework.api.util.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author neo
 */
public class SQLScriptRunner {
    private static final String DEFAULT_DELIMITER = ";";
    private static final Pattern NEW_DELIMITER_PATTERN = Pattern.compile("(?:--|\\/\\/|\\#)?!DELIMITER=(.+)");
    private static final Pattern COMMENT_PATTERN = Pattern.compile("^(?:--|\\/\\/|\\#).+");

    private final Logger logger = LoggerFactory.getLogger(SQLScriptRunner.class);
    private final Database database;
    private final String script;

    public SQLScriptRunner(Database database, String script) {
        this.database = database;
        this.script = script;
    }

    public void run() {
        int lineNumber = 0;
        try (BufferedReader reader = new BufferedReader(new StringReader(script))) {
            Query query = null;
            String delimiter = DEFAULT_DELIMITER;
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) break;
                lineNumber++;
                if (query == null) {
                    query = new Query("");
                }
                String trimmedLine = line.trim();
                Matcher delimiterMatcher = NEW_DELIMITER_PATTERN.matcher(trimmedLine);
                Matcher commentMatcher = COMMENT_PATTERN.matcher(trimmedLine);
                if (delimiterMatcher.find()) {
                    delimiter = delimiterMatcher.group(1);
                } else if (!commentMatcher.find()) {
                    query.appendStatement(trimmedLine);
                    query.appendStatement(" ");
                    if (trimmedLine.endsWith(delimiter)) {
                        logger.info("execute, sql={}", query.statement());
                        database.execute(query);
                        query = null;
                    }
                }
            }
        } catch (RuntimeException | IOException e) {
            throw Exceptions.error("failed to run script, error={}, lineNumber={}", e.getMessage(), lineNumber, e);
        }
    }
}
