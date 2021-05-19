package org.acme.callcenter.messaging.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.message.StopSolverEvent;
import org.acme.callcenter.service.SolverService;

@ApplicationScoped
public class StopSolverEventHandler extends AbstractEventHandler<StopSolverEvent> {

    @Inject
    SolverService solverService;

    @Override
    public <T extends SolverEvent> boolean supports(Class<T> type) {
        return StopSolverEvent.class.isAssignableFrom(type);
    }

    @Override
    public void handleEventInternally(StopSolverEvent event) {
        solverService.stopSolving(event.getProblemId());
    }
}
