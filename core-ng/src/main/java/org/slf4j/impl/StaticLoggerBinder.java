package org.slf4j.impl;

import core.framework.impl.log.DefaultLoggerFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * @author neo
 */
public final class StaticLoggerBinder implements LoggerFactoryBinder {
    public static final String REQUESTED_API_VERSION = "1.7";
    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    private final ILoggerFactory loggerFactory = new DefaultLoggerFactory();

    private StaticLoggerBinder() {
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return DefaultLoggerFactory.class.getName();
    }
}
