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
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.message.AddCallEvent;
import org.acme.callcenter.message.BestSolutionEvent;
import org.acme.callcenter.message.ErrorEvent;
import org.acme.callcenter.message.ProlongCallEvent;
import org.acme.callcenter.message.RemoveCallEvent;
import org.acme.callcenter.message.StartSolverEvent;
import org.acme.callcenter.message.StopSolverEvent;
import org.acme.callcenter.persistence.CallCenterRepository;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;

import io.smallrye.common.annotation.Blocking;

@ApplicationScoped
public class SolverMessageHandler {

    private static final Duration CALL_PROLONGATION_DURATION = Duration.ofMinutes(1L);

    private Consumer<CallCenter> bestSolutionConsumer;

    private AtomicBoolean solving = new AtomicBoolean(false);

    @Inject
    CallCenterRepository callCenterRepository;

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
    @Blocking
    public void handleBestSolutionEvent(BestSolutionEvent bestSolutionEvent) {
        long eventProblemId = Objects.requireNonNull(bestSolutionEvent.getProblemId());
        if (DataGenerator.PROBLEM_ID == eventProblemId && isSolving()) {
            if (bestSolutionConsumer == null) {
                throw new IllegalStateException(
                        "Impossible state: no best solution should be accepted before starting the solver.");
            }

            Optional<CallCenter> callCenter = callCenterRepository.load(eventProblemId);
            if (callCenter.isEmpty()) {
                throw new IllegalStateException(
                        "Cannot find a best solution with the problem ID (" + eventProblemId + ") in the repository.");
            } else {
                bestSolutionConsumer.accept(callCenter.get());
            }
        }
    }

    @Incoming("error")
    @Acknowledgment(Acknowledgment.Strategy.PRE_PROCESSING)
    public CompletionStage<Void> handleErrorEvent(Message<ErrorEvent> errorEventMessage) {
        ErrorEvent errorEvent = errorEventMessage.getPayload();
        long eventProblemId = Objects.requireNonNull(errorEvent.getProblemId());
        if (DataGenerator.PROBLEM_ID == eventProblemId) { // Error relevant to this client. Fail fast.
            errorEventMessage.ack();
            throw new IllegalStateException("Solving failed with exception class ("
                    + errorEvent.getExceptionClassName()
                    + ") and message (" + errorEvent.getExceptionMessage() + ").");

        } else { // The message has been read, but does not concern this client.
            return errorEventMessage.ack();
        }
    }

    public void startSolving(long problemId, Consumer<CallCenter> bestSolutionConsumer) {
        solving.set(true);
        this.bestSolutionConsumer = bestSolutionConsumer;
        StartSolverEvent startSolverEvent = new StartSolverEvent(problemId);
        startSolverEventEmitter.send(startSolverEvent);
    }

    public void stopSolving(long problemId) {
        solving.set(false);
        bestSolutionConsumer = null;
        StopSolverEvent stopSolverEvent = new StopSolverEvent(problemId);
        stopSolverEventEmitter.send(stopSolverEvent);
    }

    public void addCall(long problemId, Call call) {
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

    public boolean isSolving() {
        return solving.get();
    }
}
