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

package org.acme.callcenter.service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.message.AddCallEvent;
import org.acme.callcenter.message.BestSolutionEvent;
import org.acme.callcenter.message.ProlongCallEvent;
import org.acme.callcenter.message.RemoveCallEvent;
import org.acme.callcenter.message.StartSolverEvent;
import org.acme.callcenter.message.StopSolverEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

@ApplicationScoped
public class SolverService {

    private static final Duration CALL_PROLONGATION_DURATION = Duration.ofMinutes(1L);

    private final Map<Long, Consumer<CallCenter>> bestSolutionConsumers = new ConcurrentHashMap<>();

    @Inject
    @Channel("start_solver")
    Emitter<StartSolverEvent> startSolverEventEmitter;

    @Inject
    @Channel("stop_solver")
    Emitter<StopSolverEvent> stopSolverEventEmitter;

    @Inject
    @Channel("add_call")
    Emitter<AddCallEvent> addCallEventEmitter;

    @Inject
    @Channel("remove_call")
    Emitter<RemoveCallEvent> removeCallEventEmitter;

    @Inject
    @Channel("prolong_call")
    Emitter<ProlongCallEvent> prolongCallEventEmitter;

    @Incoming("best_solution")
    public void handleBestSolutionEvent(BestSolutionEvent bestSolutionEvent) {
        if (bestSolutionEvent == null) {
            System.out.println("Null bestSolutionEvent");
            return;
        }
//        Objects.requireNonNull(bestSolutionEvent);
        // TODO: what if there is no consumer?
        Consumer<CallCenter> bestSolutionConsumer = bestSolutionConsumers.get(bestSolutionEvent.getProblemId());
        if (bestSolutionConsumer == null) {
            System.out.println("No best solution consumer found for problem ID (" + bestSolutionEvent.getProblemId() + ")");
            //throw new IllegalStateException("No best solution consumer found for problem ID (" + bestSolutionEvent.getProblemId() + ")");
        } else {
            bestSolutionConsumer.accept(bestSolutionEvent.getBestSolution());
        }
    }

    public void startSolving(long problemId, CallCenter inputProblem, Consumer<CallCenter> bestSolutionConsumer) {
        bestSolutionConsumers.put(problemId, bestSolutionConsumer);
        StartSolverEvent startSolverEvent = new StartSolverEvent(problemId, inputProblem);
        startSolverEventEmitter.send(startSolverEvent);
    }

    public void stopSolving(long problemId) {
        bestSolutionConsumers.remove(problemId);
        StopSolverEvent stopSolverEvent = new StopSolverEvent(problemId);
        stopSolverEventEmitter.send(stopSolverEvent);
    }

    public void addCall(long problemId, Call call) {
        System.out.println("Automatically creating a call: " + call.getId());
        AddCallEvent addCallEvent = new AddCallEvent(problemId, call);
        addCallEventEmitter.send(addCallEvent);
    }

    public void removeCall(long problemId, long callId) {
        RemoveCallEvent removeCallEvent = new RemoveCallEvent(problemId, callId);
        removeCallEventEmitter.send(removeCallEvent);
    }

    public void prolongCall(long problemId, long callId) {
        ProlongCallEvent prolongCallEvent = new ProlongCallEvent(problemId, callId, CALL_PROLONGATION_DURATION);
        prolongCallEventEmitter.send(prolongCallEvent);
    }
}
