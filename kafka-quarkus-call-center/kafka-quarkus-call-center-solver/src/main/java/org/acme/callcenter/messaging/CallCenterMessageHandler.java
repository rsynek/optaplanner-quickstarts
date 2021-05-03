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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.AddAgentEvent;
import org.acme.callcenter.message.AddCallEvent;
import org.acme.callcenter.message.BestSolutionEvent;
import org.acme.callcenter.message.StartSolverEvent;
import org.acme.callcenter.message.StopSolverEvent;
import org.acme.callcenter.solver.SolverService;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class CallCenterMessageHandler {

    @Inject
    SolverService solverService;

    @Inject
    @Channel("best_solution")
    Emitter<BestSolutionEvent> bestSolutionEmitter;

    @Incoming("add_agent")
    public void handleAddAgent(AddAgentEvent addAgentEvent) {
        solverService.addAgent(addAgentEvent.getAgent());
    }

    @Incoming("add_call")
    public void handleAddCall(AddCallEvent addCallEvent) {
        solverService.addCall(addCallEvent.getCall());
    }

    @Incoming("start_solver")
    public void handleStartSolver(StartSolverEvent startSolverEvent) {
        solverService.startSolving(startSolverEvent.getInputProblem(),
                bestSolutionChangedEvent -> {
                    bestSolutionEmitter.send(new BestSolutionEvent(startSolverEvent.getProblemId(),
                            bestSolutionChangedEvent.getNewBestSolution()));
                }, throwable -> throwable.printStackTrace());
    }

    @Incoming("stop_solver")
    public void handleSolverCommand(StopSolverEvent stopSolverEvent) {
        solverService.stopSolving();
    }

}
