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

package org.acme.callcenter.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.acme.callcenter.change.AddCallProblemFactChange;
import org.acme.callcenter.change.ProlongCallProblemFactChange;
import org.acme.callcenter.change.RemoveCallProblemFactChange;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.core.api.solver.SolverFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SolverManagerTest {

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    SolverFactory<CallCenter> solverFactory;

    private SolverManager solverManager;

    private static final Set<Skill> SKILLS = EnumSet.of(Skill.ENGLISH, Skill.CAR_INSURANCE);
    private static final Duration CALL_DURATION = Duration.ofSeconds(10);
    private static final long PROBLEM_ID = 1L;

    @BeforeEach
    void setUp() {
        solverManager = new SolverManager(solverFactory, managedExecutor);
    }

    @Test
    @Timeout(60)
    void addCall() {
        Call call1 = new Call(1L, "123-456-7891", SKILLS, CALL_DURATION);
        Call call2 = new Call(2L, "123-456-7892", SKILLS, CALL_DURATION);
        CallCenter bestSolution = solve(createInputProblem(), () -> {
            AddCallProblemFactChange addCall1 = new AddCallProblemFactChange(PROBLEM_ID, call1);
            AddCallProblemFactChange addCall2 = new AddCallProblemFactChange(PROBLEM_ID, call2);
            solverManager.registerProblemFactChange(addCall1);
            solverManager.registerProblemFactChange(addCall2);
        });

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);

        assertThat(agentWithCalls.getAssignedCalls())
                .containsExactlyInAnyOrder(call1, call2);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);
    }

    @Test
    @Timeout(60)
    void prolongCall() {
        CallCenter inputProblem = createInputProblem();
        Call call1 = new Call(1L, "123-456-7891", SKILLS, CALL_DURATION);
        Call call2 = new Call(2L, "123-456-7892", SKILLS, CALL_DURATION);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);

        CallCenter bestSolution = solve(inputProblem, () -> {
            ProlongCallProblemFactChange prolongCallProblemFactChange =
                    new ProlongCallProblemFactChange(PROBLEM_ID, call1.getId(), Duration.ofMinutes(1));
            solverManager.registerProblemFactChange(prolongCallProblemFactChange);
        });

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);

        assertThat(agentWithCalls.getAssignedCalls()).hasSize(2);
        Call prolongedCall = agentWithCalls.getAssignedCalls().stream()
                .filter(call -> call.getId().equals(call1.getId()))
                .findFirst()
                .orElseGet(() -> Assertions.fail("The expected prolonged call has not been found."));
        assertThat(prolongedCall.getDuration()).hasMinutes(1L);
        assertThat(prolongedCall.getDurationTillPickUp()).hasMinutes(1L);
    }

    @Test
    @Timeout(60)
    void removeCall() {
        CallCenter inputProblem = createInputProblem();
        Call call1 = new Call(1L, "123-456-7891", Skill.ENGLISH, Skill.CAR_INSURANCE);
        Call call2 = new Call(2L, "123-456-7892", Skill.ENGLISH, Skill.CAR_INSURANCE);
        inputProblem.getCalls().add(call1);
        inputProblem.getCalls().add(call2);

        CallCenter bestSolution = solve(inputProblem, () -> {
            RemoveCallProblemFactChange removeCallProblemFactChange =
                    new RemoveCallProblemFactChange(PROBLEM_ID, call1.getId());
            solverManager.registerProblemFactChange(removeCallProblemFactChange);
        });

        Agent agentWithCalls = getFirstAgentWithCallOrFail(bestSolution);
        assertThat(agentWithCalls.getSkills()).contains(Skill.ENGLISH, Skill.CAR_INSURANCE);

        assertThat(agentWithCalls.getAssignedCalls()).hasSize(1);
        Call call = agentWithCalls.getAssignedCalls().get(0);
        assertThat(call.getId()).isEqualTo(call2.getId());
    }

    private CallCenter createInputProblem() {
        Agent agent = new Agent(1L, "Alice", SKILLS);
        List<Agent> agents = Collections.singletonList(agent);
        return new CallCenter(agents, new ArrayList<>());
    }

    private CallCenter solve(CallCenter inputProblem, Runnable problemChanges) {
        CountDownLatch allChangesProcessed = new CountDownLatch(1);

        AtomicReference<Throwable> errorDuringSolving = new AtomicReference<>();
        AtomicReference<CallCenter> bestSolution = new AtomicReference<>();
        solverManager.startSolving(inputProblem, bestSolutionChangedEvent -> {
            bestSolution.set(bestSolutionChangedEvent.getNewBestSolution());
            allChangesProcessed.countDown();
            solverManager.stopSolving();
        }, throwable -> errorDuringSolving.set(throwable));

        problemChanges.run();

        try {
            while (!allChangesProcessed.await(10, TimeUnit.MILLISECONDS)) {
                if (errorDuringSolving.get() != null) {
                    throw new IllegalStateException("Exception during solving", errorDuringSolving.get());
                }
            }
        } catch (InterruptedException interruptedException) {
            throw new IllegalStateException("Interrupted during waiting.", interruptedException);
        }

        return bestSolution.get();
    }

    private Agent getFirstAgentWithCallOrFail(CallCenter callCenter) {
        return callCenter.getAgents().stream()
                .filter(agent -> !agent.getAssignedCalls().isEmpty())
                .findFirst()
                .orElseGet(() -> Assertions.fail("There is no agent with assigned calls."));
    }
}
