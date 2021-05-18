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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.callcenter.InMemoryMessagingConnectorResource;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;
import org.acme.callcenter.message.BestSolutionEvent;
import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.persistence.CallCenterRepository;
import org.acme.callcenter.persistence.ProblemFactChangeRepository;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;

@QuarkusTest
@QuarkusTestResource(InMemoryMessagingConnectorResource.class)
public class SolverServiceTest {

    private static final long PROBLEM_ID = 1L;

    @Any
    @Inject
    InMemoryConnector connector;

    @Inject
    ProblemFactChangeRepository problemFactChangeRepository;

    @Inject
    CallCenterRepository callCenterRepository;

    @Inject
    SolverService solverService;

    @Test
    @TestTransaction
    void start_when_problem_already_active_skip() {
        CallCenterRepository callCenterRepositoryMock = Mockito.mock(CallCenterRepository.class);
        QuarkusMock.installMockForType(callCenterRepositoryMock, CallCenterRepository.class);

        when(callCenterRepositoryMock.load(PROBLEM_ID)).thenReturn(Optional.of(new CallCenter()));
        when(callCenterRepositoryMock.compareAndSetState(PROBLEM_ID, false, true)).thenReturn(false);
        solverService.startSolving(PROBLEM_ID);
        assertThat(solverService.isSolvingProblem(PROBLEM_ID)).isFalse();
    }

    @Test
    @TestTransaction
    void start_no_input_problem_found() {
        CallCenterRepository callCenterRepositoryMock = Mockito.mock(CallCenterRepository.class);
        QuarkusMock.installMockForType(callCenterRepositoryMock, CallCenterRepository.class);

        when(callCenterRepositoryMock.load(PROBLEM_ID)).thenReturn(Optional.empty());
        assertThat(solverService.isSolvingProblem(PROBLEM_ID)).isFalse();
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(() -> solverService.startSolving(PROBLEM_ID))
                .withMessage("Cannot find an input problem with the problemId (" + PROBLEM_ID + ") in the repository.");
    }

    @Test
    void start_produce_changes_check_solution() {
        // Save input problem to a DB.
        Set<Skill> skills = EnumSet.of(Skill.CAR_INSURANCE, Skill.ENGLISH);
        Agent agent = new Agent(1L, "Bob", skills);
        CallCenter inputProblem = new CallCenter(Collections.singletonList(agent), new ArrayList<>());
        callCenterRepository.save(PROBLEM_ID, inputProblem);

        // Start solving and send problem fact changes.
        solverService.startSolving(PROBLEM_ID);

        Call call1 = new Call(1L, "126-498-784", skills, Duration.ofSeconds(10));
        Call call2 = new Call(2L, "657-787-246", skills, Duration.ofSeconds(10));
        solverService.addCall(PROBLEM_ID, call1);
        solverService.addCall(PROBLEM_ID, call2);

        // Wait for the new best solution.
        await()
                .timeout(Duration.ofSeconds(5))
                .until(() -> {
                    Optional<CallCenter> solution = callCenterRepository.load(PROBLEM_ID);
                    if (solution.isPresent()) {
                        Long lastChangeId = solution.get().getLastChangeId();
                        return lastChangeId != null && lastChangeId == 2L;
                    }
                    return false;
                });

        Optional<CallCenter> solutionOptional = callCenterRepository.load(PROBLEM_ID);
        assertThat(solutionOptional).isNotEmpty();
        assertThat(problemFactChangeRepository.findByIdGreaterThan(PROBLEM_ID, 0L)).isEmpty();

        InMemorySink<BestSolutionEvent> bestSolutionChannel = connector.sink(CallCenterChannelNames.BEST_SOLUTION);
        List<? extends Message<BestSolutionEvent>> receivedEvents = bestSolutionChannel.received();
        assertThat(receivedEvents).isNotEmpty();
        BestSolutionEvent bestSolutionEvent = receivedEvents.get(0).getPayload();
        assertThat(bestSolutionEvent.getProblemId()).isEqualTo(PROBLEM_ID);
    }
}
