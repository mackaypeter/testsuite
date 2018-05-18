package org.jboss.hal.testsuite.test.configuration.infinispan;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.container.CacheContainerContext;
import org.jboss.hal.testsuite.test.configuration.infinispan.cache.type.CacheContext;
import org.jboss.hal.testsuite.util.ConfigChecker;
import org.jboss.hal.testsuite.util.junit.ArquillianParametrized;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.wildfly.extras.creaper.core.online.operations.Address;

import java.util.Collection;

@RunWith(ArquillianParametrized.class)
@RunAsClient
public class EvictionTestCase extends InfinispanTestCaseAbstract {

    private static final String EVICTION = "eviction";
    private static final String EVICTION_LABEL = "Eviction";
    private static final String COMPONENT = "component";
    private static final String MAX_ENTRIES = "max-entries";
    private static final String STRATEGY = "strategy";

    public CacheContainerContext cacheContainerContext;
    public CacheContext cacheContext;

    public EvictionTestCase(CacheContainerContext cacheContainerContext, CacheContext cacheContext) {
        this.cacheContainerContext = cacheContainerContext;
        this.cacheContext = cacheContext;
    }

    @Parameterized.Parameters(name = "Cache container: {0}, Cache type: {1}")
    public static Collection parameters() {
        return new ParametersFactory(client).containerTypeMatrix();
    }


    @Test
    public void editEvictionTest() throws Exception {
        final long maxEntries = 1000;
        final StrategyType strategy = StrategyType.FIFO;
        final Address evictionAddress = cacheContext.getCacheAddress().and(COMPONENT, EVICTION);
        try {
            cacheContainerContext.createCacheContainerInModel();
            cacheContext.createCacheInModel();
            administration.reloadIfRequired();
            cacheContainerContext.navigateToCacheContainer(page);
            cacheContext.navigateToCache(page);
            page.getConfig().switchTo(EVICTION_LABEL);
            new ConfigChecker.Builder(client, evictionAddress)
                    .configFragment(page.getConfigFragment())
                    .edit(ConfigChecker.InputType.TEXT, MAX_ENTRIES, String.valueOf(maxEntries))
                    .edit(ConfigChecker.InputType.SELECT, STRATEGY, strategy.getStrategyValue())
                    .andSave()
                    .verifyFormSaved()
                    .verifyAttribute(STRATEGY, strategy.getStrategyValue())
                    .verifyAttribute(MAX_ENTRIES, maxEntries);
        } finally {
            cacheContext.removeCacheInModel();
            cacheContainerContext.removeCacheContainerInModel();
            administration.reloadIfRequired();
        }
    }
}