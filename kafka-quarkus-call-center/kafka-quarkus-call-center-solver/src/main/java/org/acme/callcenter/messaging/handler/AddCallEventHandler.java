package org.acme.callcenter.messaging.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.AddCallEvent;
import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.service.SolverService;

@ApplicationScoped
public class AddCallEventHandler extends AbstractEventHandler<AddCallEvent> {

    @Inject
    SolverService solverService;

    @Override
    public <T extends SolverEvent> boolean supports(Class<T> type) {
        return AddCallEvent.class.isAssignableFrom(type);
    }

    @Override
    protected void handleEventInternally(AddCallEvent event) {
        solverService.addCall(event.getProblemId(), event.getCall());
    }
}
