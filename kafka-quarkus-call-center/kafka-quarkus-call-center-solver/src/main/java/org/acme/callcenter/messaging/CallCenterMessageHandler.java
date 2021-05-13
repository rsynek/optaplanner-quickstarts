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
import javax.inject.Inject;

import org.acme.callcenter.message.AddCallEvent;
import org.acme.callcenter.message.ProlongCallEvent;
import org.acme.callcenter.message.RemoveCallEvent;
import org.acme.callcenter.message.StartSolverEvent;
import org.acme.callcenter.message.StopSolverEvent;
import org.acme.callcenter.persistence.ProblemFactChangeRepository;
import org.acme.callcenter.service.SolverService;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class CallCenterMessageHandler {

    @Inject
    ProblemFactChangeRepository problemFactChangeRepository;

    @Inject
    SolverService solverService;

    @Incoming("add_call")
    @Blocking
    public void handleAddCall(AddCallEvent addCallEvent) {
        long eventProblemId = Objects.requireNonNull(addCallEvent).getProblemId();
        if (solverService.isSolvingProblem(eventProblemId)) {
            solverService.addCall(eventProblemId, addCallEvent.getCall());
        }
    }

    @Incoming("remove_call")
    @Blocking
    public void handleRemoveCall(RemoveCallEvent removeCallEvent) {
        long eventProblemId = Objects.requireNonNull(removeCallEvent).getProblemId();
        if (solverService.isSolvingProblem(eventProblemId)) {
            solverService.removeCall(eventProblemId, removeCallEvent.getCallId());
        }
    }

    @Incoming("prolong_call")
    @Blocking
    public void handleProlongCall(ProlongCallEvent prolongCallEvent) {
        long eventProblemId = Objects.requireNonNull(prolongCallEvent).getProblemId();
        if (solverService.isSolvingProblem(eventProblemId)) {
            solverService.prolongCall(eventProblemId, prolongCallEvent.getCallId(), prolongCallEvent.getProlongation());
        }
    }

    @Incoming("start_solver")
    @Blocking
    public void handleStartSolver(StartSolverEvent startSolverEvent) {
        long eventProblemId = Objects.requireNonNull(startSolverEvent).getProblemId();
        solverService.startSolving(eventProblemId);
    }

    @Incoming("stop_solver")
    @Blocking
    public void handleStopSolver(StopSolverEvent stopSolverEvent) {
        long eventProblemId = Objects.requireNonNull(stopSolverEvent).getProblemId();
        solverService.stopSolving(eventProblemId);
    }
}
