package core.diagram.web;

import core.diagram.service.Diagram;
import core.framework.json.JSON;

/**
 * @author neo
 */
public class DiagramModel {
    public String title;
    public Diagram diagram;

    public String dot() {
        return diagram.dot;
    }

    public String tooltips() {
        return JSON.toJSON(diagram.tooltips);
    }
}
