import core.framework.impl.log.DefaultLoggerServiceProvider;
import org.slf4j.spi.SLF4JServiceProvider;

module core.framework {
    requires transitive core.framework.api;
    requires transitive java.sql;
    requires transitive org.slf4j;
    requires java.management;
    requires java.net.http;
    requires xnio.api;
    requires undertow.core;
    requires jackson.annotations;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires com.fasterxml.jackson.module.afterburner;
    requires javassist;
    requires static kafka.clients;

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

    provides SLF4JServiceProvider with DefaultLoggerServiceProvider;
}
