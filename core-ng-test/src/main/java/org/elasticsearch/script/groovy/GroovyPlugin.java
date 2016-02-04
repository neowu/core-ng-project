package org.elasticsearch.script.groovy;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.script.ScriptModule;

// https://github.com/elastic/elasticsearch/blob/master/modules/lang-groovy/src/main/java/org/elasticsearch/script/groovy/GroovyPlugin.java
public class GroovyPlugin extends Plugin {

    @Override
    public String name() {
        return "lang-groovy";
    }

    @Override
    public String description() {
        return "Groovy scripting integration for Elasticsearch";
    }

    public void onModule(ScriptModule module) {
        module.addScriptEngine(GroovyScriptEngineService.class);
    }
}