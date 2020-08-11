package org.drools.core.addon;

import org.drools.core.util.ClassUtils;
import org.kie.api.conf.AlphaNodeOrderingOption;
import org.kie.api.internal.utils.ServiceRegistry;

public interface AlphaNodeOrderingStrategyProvider {

    AlphaNodeOrderingStrategy createCountBasedOrderingStrategy();

    AlphaNodeOrderingStrategy createCustomOrderingStrategy();

    AlphaNodeOrderingStrategy createNoopOrderingStrategy();

    static AlphaNodeOrderingStrategy createAlphaNodeOrderingStrategy(AlphaNodeOrderingOption option) {
        if (AlphaNodeOrderingOption.COUNT.equals(option)) {
            return Factory.get().createCountBasedOrderingStrategy();
        } else if (AlphaNodeOrderingOption.CUSTOM.equals(option)) {
            return Factory.get().createCustomOrderingStrategy();
        } else if (AlphaNodeOrderingOption.NONE.equals(option)) {
            return Factory.get().createNoopOrderingStrategy();
        } else {
            throw new IllegalArgumentException("No implementation found for AlphaNodeOrderingOption [" + option + "]");
        }
    }

    class DrlAlphaNodeOrderingStrategyProvider implements AlphaNodeOrderingStrategyProvider {

        @Override
        public AlphaNodeOrderingStrategy createCountBasedOrderingStrategy() {
            return new CountBasedOrderingStrategy();
        }

        @Override
        public AlphaNodeOrderingStrategy createCustomOrderingStrategy() {
            String customStrategyClassName = System.getProperty(AlphaNodeOrderingOption.CUSTOM_CLASS_PROPERTY_NAME);
            if (customStrategyClassName == null || customStrategyClassName.trim().isEmpty()) {
                throw new RuntimeException("Configure system property " + AlphaNodeOrderingOption.CUSTOM_CLASS_PROPERTY_NAME + " with custom strategy implementation FQCN when you use AlphaNodeOrderingOption.CUSTOM");
            } else {
                return (AlphaNodeOrderingStrategy) ClassUtils.instantiateObject(customStrategyClassName);
            }
        }

        @Override
        public AlphaNodeOrderingStrategy createNoopOrderingStrategy() {
            return new NoopOrderingStrategy();
        }
    }

    class Factory {

        private static class LazyHolder {

            private static final AlphaNodeOrderingStrategyProvider INSTANCE = createProvider();

            private static AlphaNodeOrderingStrategyProvider createProvider() {
                AlphaNodeOrderingStrategyProvider provider = ServiceRegistry.getInstance().get(AlphaNodeOrderingStrategyProvider.class);
                return provider != null ? provider : new DrlAlphaNodeOrderingStrategyProvider();
            }
        }

        public static AlphaNodeOrderingStrategyProvider get() {
            return LazyHolder.INSTANCE;
        }

        private Factory() {}
    }
}
