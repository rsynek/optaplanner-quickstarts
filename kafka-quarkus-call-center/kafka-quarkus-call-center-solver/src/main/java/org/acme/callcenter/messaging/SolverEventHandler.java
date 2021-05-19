package org.acme.callcenter.messaging;

import org.acme.callcenter.message.SolverEvent;

public interface SolverEventHandler {

    <T extends SolverEvent> boolean supports(Class<T> type);

    void handleEvent(SolverEvent event);
}
