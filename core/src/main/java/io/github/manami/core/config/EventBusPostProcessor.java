package io.github.manami.core.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

/**
 * @author manami project
 * @since 2.7.2
 */
@Named
public class EventBusPostProcessor implements BeanPostProcessor {

    private final EventBus eventBus;


    @Inject
    public EventBusPostProcessor(final EventBus eventBus) {
        this.eventBus = eventBus;
    }


    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }


    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        final Class<?> clazz = bean.getClass();
        final Method[] methods = clazz.getMethods();

        for (final Method method : methods) {
            final Annotation[] annotations = method.getAnnotations();

            for (final Annotation annotation : annotations) {
                final Class<? extends Annotation> annotationType = annotation.annotationType();
                final boolean subscriber = annotationType.equals(Subscribe.class);
                if (subscriber) {
                    eventBus.register(bean);
                    return bean;
                }
            }
        }

        return bean;
    }
}
