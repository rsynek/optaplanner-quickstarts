package org.acme.callcenter.messaging.handler;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.message.StartSolverEvent;
import org.acme.callcenter.service.SolverService;

@ApplicationScoped
public class StartSolverEventHandler extends AbstractEventHandler<StartSolverEvent> {

    @Inject
    SolverService solverService;

    @Override
    public <T extends SolverEvent> boolean supports(Class<T> type) {
        return StartSolverEvent.class.isAssignableFrom(type);
    }

    @Override
    protected void handleEventInternally(StartSolverEvent event) {
        solverService.startSolving(event.getProblemId());
    }
}
