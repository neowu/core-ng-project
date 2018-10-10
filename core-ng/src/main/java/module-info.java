import core.framework.impl.log.DefaultLoggerServiceProvider;

/**
 * @author neo
 */
module core.ng {
    requires java.management;
    requires java.net.http;
    requires transitive java.sql;
    requires transitive core.ng.api;
    requires transitive org.slf4j;
    requires undertow.core;
    requires transitive com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.afterburner;

    exports core.framework.async;
    exports core.framework.cache;
    exports core.framework.crypto;
    exports core.framework.db;
    exports core.framework.http;
    exports core.framework.inject;
    exports core.framework.json;
    exports core.framework.kafka;
    exports core.framework.log;
    exports core.framework.module;
    exports core.framework.redis;
    exports core.framework.scheduler;
    exports core.framework.template;
    exports core.framework.util;
    exports core.framework.web;
    exports core.framework.web.exception;
    exports core.framework.web.rate;
    exports core.framework.web.service;
    exports core.framework.web.site;
    exports core.framework.web.websocket;

    provides org.slf4j.spi.SLF4JServiceProvider with DefaultLoggerServiceProvider;
}
