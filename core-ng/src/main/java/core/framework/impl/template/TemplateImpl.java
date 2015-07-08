package core.framework.impl.template;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * @author neo
 */
public class TemplateImpl {
    public String process() {
        Document doc = Jsoup.parse("");
        StringBuilder builder = new StringBuilder();

        for (Element element : doc.getAllElements()) {

        }
        return builder.toString();
    }
}
