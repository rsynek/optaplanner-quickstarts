package org.acme.callcenter.messaging.handler;

import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.messaging.SolverEventHandler;

abstract class AbstractEventHandler<T extends SolverEvent> implements SolverEventHandler {

    @Override
    public void handleEvent(SolverEvent event) {
        if (!supports(event.getClass())) {
            throw new IllegalArgumentException(event.getClass().getName() + " is not supported by the handler (" + getClass().getName() + ")");
        }
        handleEventInternally((T) event);
    }

    protected abstract void handleEventInternally(T event);
}
