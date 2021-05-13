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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.callcenter.change.PersistableProblemFactChange;
import org.acme.callcenter.change.ProblemFactChangeFactory;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.messaging.CallCenterMessageSender;
import org.acme.callcenter.persistence.CallCenterRepository;
import org.acme.callcenter.persistence.ProblemFactChangeRepository;
import org.acme.callcenter.solver.SolverManager;

import io.quarkus.runtime.StartupEvent;

public class SolverService {

    private static final int NOT_SOLVING_PROBLEM_ID = -1;

    @Inject
    SolverManager solverManager;

    @Inject
    CallCenterRepository callCenterRepository;

    @Inject
    ProblemFactChangeRepository problemFactChangeRepository;

    @Inject
    CallCenterMessageSender messageSender;

    private final AtomicLong currentProblemId = new AtomicLong(NOT_SOLVING_PROBLEM_ID);

    public boolean isSolvingProblem(long problemId) {
        return currentProblemId.get() == problemId;
    }

    public synchronized void startSolving(long problemId) {
        if (currentProblemId.get() != NOT_SOLVING_PROBLEM_ID) { // Skip if this pod is already solving a different problemId.
            return;
        }
        Optional<CallCenter> inputProblemOptional = callCenterRepository.load(problemId);
        if (inputProblemOptional.isEmpty()) {
            throw new NoSuchElementException(
                    "Cannot find an input problem with the problemId (" + problemId + ") in the repository.");
        }

        // Check if the problemId has been already taken by a different pod.
        if (callCenterRepository.compareAndSetState(problemId, false, true)) {
            currentProblemId.set(problemId);
            CallCenter inputProblem = inputProblemOptional.get();
            solverManager.startSolving(inputProblem,
                    bestSolutionChangedEvent -> {
                        persistBestSolution(problemId, bestSolutionChangedEvent.getNewBestSolution());
                        messageSender.sendBestSolutionEvent(problemId);
                    }, throwable -> { // TODO: send to the client.
                        throw new CompletionException(throwable);
                    });
            applyWaitingProblemFactChanges(problemId, inputProblem.getLastChangeId());
        }
    }

    private void applyWaitingProblemFactChanges(long problemId, Long lastChangeId) {
        if (lastChangeId != null) {
            List<PersistableProblemFactChange> problemFactChanges =
                    problemFactChangeRepository.findByIdGreaterThan(problemId, lastChangeId);
            solverManager.registerProblemFactChanges(problemFactChanges.stream().collect(Collectors.toList()));
        }
    }

    public void addCall(long problemId, Call call) {
        PersistableProblemFactChange addCallProblemFactChange =
                ProblemFactChangeFactory.addCallProblemFactChange(problemId, call);
        problemFactChangeRepository.save(addCallProblemFactChange);
        solverManager.registerProblemFactChange(addCallProblemFactChange);
    }

    public void removeCall(long problemId, long callId) {
        PersistableProblemFactChange removeCallProblemFactChange =
                ProblemFactChangeFactory.removeCallProblemFactChange(problemId, callId);
        problemFactChangeRepository.save(removeCallProblemFactChange);
        solverManager.registerProblemFactChange(removeCallProblemFactChange);
    }

    public void prolongCall(long problemId, long callId, Duration prolongation) {
        PersistableProblemFactChange prolongCallProblemFactChange =
                ProblemFactChangeFactory.prolongCallProblemFactChange(problemId, callId, prolongation);
        problemFactChangeRepository.save(prolongCallProblemFactChange);
        solverManager.registerProblemFactChange(prolongCallProblemFactChange);
    }

    public void stopSolving(long problemId) {
        if (currentProblemId.compareAndSet(problemId, NOT_SOLVING_PROBLEM_ID)) {
            callCenterRepository.compareAndSetState(problemId, true, false);
            solverManager.stopSolving();
        }
    }

    public void boot(@Observes StartupEvent startupEvent) {
        // TODO: Avoid duplicate solving of the same problemId with multiple running pods.
        Optional<Long> activeCallCenterProblemId = callCenterRepository.getFirstActiveId();
        if (activeCallCenterProblemId.isPresent()) {
            long problemId = activeCallCenterProblemId.get();
            // Start solving and apply all the waiting PFCs.
            startSolving(problemId);
        }
    }

    @Transactional
    private void persistBestSolution(long problemId, CallCenter callCenter) {
        callCenterRepository.save(problemId, callCenter);
        if (callCenter.getLastChangeId() != null) {
            problemFactChangeRepository.deleteByIdLesserThanOrEqualTo(callCenter.getLastChangeId());
        }
    }
}
