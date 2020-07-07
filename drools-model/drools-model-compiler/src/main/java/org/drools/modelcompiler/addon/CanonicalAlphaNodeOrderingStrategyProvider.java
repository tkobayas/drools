package org.drools.modelcompiler.addon;

import org.drools.core.addon.AlphaNodeOrderingStrategy;
import org.drools.core.addon.AlphaNodeOrderingStrategyProvider;

public class CanonicalAlphaNodeOrderingStrategyProvider extends AlphaNodeOrderingStrategyProvider.DrlAlphaNodeOrderingStrategyProvider implements AlphaNodeOrderingStrategyProvider {

    @Override
    public AlphaNodeOrderingStrategy createCountBasedOrderingStrategy() {
        return new CanonicalCountBasedOrderingStrategy();
    }

}
