package com.jcqrslib.jcqrs.cqrs;

public interface IRequestHandler<TRequest extends IRequest<TResponse>, TResponse> {
    TResponse handle(TRequest request);

    Class<?> getRequestType();

    default boolean canHandle(Object request) {
        return getRequestType().isAssignableFrom(request.getClass());
    }
}
