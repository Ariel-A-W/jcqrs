package com.jcqrslib.jcqrs.cqrs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class Mediator {
    @Autowired
    private ApplicationContext context;

    public <TRequest extends IRequest<TResponse>, TResponse> TResponse send(TRequest request) {
        Map<String, IRequestHandler> beans = context.getBeansOfType(IRequestHandler.class);

        for (IRequestHandler handler : beans.values()) {
            if (handler.canHandle(request)) {
                return ((IRequestHandler<TRequest, TResponse>) handler).handle(request);
            }
        }

        throw new RuntimeException("No se encontró un handler que pueda procesar: " + request.getClass().getSimpleName());
    }
}
