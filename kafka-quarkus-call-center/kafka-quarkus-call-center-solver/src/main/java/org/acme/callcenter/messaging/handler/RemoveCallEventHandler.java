package org.acme.callcenter.messaging.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.RemoveCallEvent;
import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.service.SolverService;

@ApplicationScoped
public class RemoveCallEventHandler extends AbstractEventHandler<RemoveCallEvent> {

    @Inject
    SolverService solverService;

    @Override
    public <T extends SolverEvent> boolean supports(Class<T> type) {
        return RemoveCallEvent.class.isAssignableFrom(type);
    }

    @Override
    protected void handleEventInternally(RemoveCallEvent event) {
        solverService.removeCall(event.getProblemId(), event.getCallId());
    }
}
