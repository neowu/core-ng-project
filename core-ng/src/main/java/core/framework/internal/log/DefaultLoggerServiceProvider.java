package core.framework.internal.log;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

/**
 * @author neo
 */
public class DefaultLoggerServiceProvider implements SLF4JServiceProvider {
    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return "2.0.99";
    }

    @Override
    public void initialize() {
        loggerFactory = new DefaultLoggerFactory();
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new NOPMDCAdapter();
    }
}
