package com.jcqrslib.jcqrs.cqrs;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestHandlerAliasConfig implements BeanDefinitionRegistryPostProcessor {

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // Not Implemented
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanNamesForType(IRequestHandler.class);

        for (String beanName : beanNames) {
            Class<?> type = beanFactory.getType(beanName);

            if (type != null) {
                String aliasName = buildAliasName(type.getSimpleName());
                if (!beanFactory.containsBean(aliasName)) {
                    ((BeanDefinitionRegistry) beanFactory).registerAlias(beanName, aliasName);
                    System.out.println("🔗 Alias registrado: " + aliasName + " → " + beanName);
                }
            }
        }
    }

    private String buildAliasName(String className) {
        // Manipulation of interoperative classes convention.
        if (className.endsWith("Handler")) {
            return Character.toLowerCase(className.charAt(0)) +
                    className.substring(1).replace("Handler", "RequestHandler");
        }
        return className;
    }
}
