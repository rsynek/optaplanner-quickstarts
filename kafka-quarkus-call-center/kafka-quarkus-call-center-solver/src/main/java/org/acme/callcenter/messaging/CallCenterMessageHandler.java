/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.callcenter.messaging;

import java.util.Objects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.message.SolverEvent;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class CallCenterMessageHandler {

    @Inject
    Instance<SolverEventHandler> solverEventHandlers;

    @Incoming(CallCenterChannelNames.SOLVER)
    @Blocking
    public void handleSolverEvent(SolverEvent solverEvent) {
        Objects.requireNonNull(solverEvent);
        findHandler(solverEvent).handleEvent(solverEvent);
    }

    private SolverEventHandler findHandler(SolverEvent solverEvent) {
        return solverEventHandlers.stream()
                .filter(solverEventHandler -> solverEventHandler.supports(solverEvent.getClass()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unsupported solver event type (" + solverEvent.getClass() + ")."));
    }
}
