package org.acme.callcenter.messaging.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.ProlongCallEvent;
import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.service.SolverService;

@ApplicationScoped
public class ProlongCallEventHandler extends AbstractEventHandler<ProlongCallEvent> {

    @Inject
    SolverService solverService;

    @Override
    public <T extends SolverEvent> boolean supports(Class<T> type) {
        return ProlongCallEvent.class.isAssignableFrom(type);
    }

    @Override
    protected void handleEventInternally(ProlongCallEvent event) {
        solverService.prolongCall(event.getProblemId(), event.getCallId(), event.getProlongation());
    }
}
