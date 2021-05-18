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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.optaplanner.core.api.solver.ProblemFactChange;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.api.solver.event.BestSolutionChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class SolverManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SolverManager.class);

    private final ManagedExecutor managedExecutor;

    private final SolverFactory<CallCenter> solverFactory;
    // TODO: Replace by @Inject SolverManager once https://issues.redhat.com/browse/PLANNER-2141 is resolved.
    private Solver<CallCenter> solver;

    private AtomicBoolean solving = new AtomicBoolean(false);
    private CompletableFuture<?> completableSolverFuture;
    // TODO: Might not be needed anymore thanks to persistence.
    private final BlockingQueue<ProblemFactChange<CallCenter>> waitingProblemFactChanges = new LinkedBlockingQueue<>();

    @Inject
    public SolverManager(SolverFactory<CallCenter> solverFactory, @Default ManagedExecutor executorService) {
        this.solverFactory = solverFactory;
        this.managedExecutor = executorService;
    }

    /**
     * Pins first calls assigned to agents. Changes the best solution and applies the planning pin to working solution
     * via {@link PinCallProblemFactChange}.
     * @param bestSolution best solution that might contain calls which should be pinned.
     */
    private void pinCallAssignedToAgents(CallCenter bestSolution) {
        Collection<Call> calls = bestSolution.getCalls();
        List<ProblemFactChange<CallCenter>> pinCallProblemFactChanges = new ArrayList<>();
        calls.forEach(call -> {
            if (!call.isPinned()
                    && call.getPreviousCallOrAgent() != null
                    && call.getPreviousCallOrAgent() instanceof Agent) {
                call.setPinned(true);
                call.setPickUpTime(LocalTime.now());
                pinCallProblemFactChanges.add(new PinCallProblemFactChange(call));
            }
        });
        solver.addProblemFactChanges(pinCallProblemFactChanges);
    }

    public synchronized void startSolving(CallCenter inputProblem,
                                          Consumer<BestSolutionChangedEvent<CallCenter>> bestSolutionChangedEventConsumer, Consumer<Throwable> errorHandler) {
        if (isSolving()) {
            throw new IllegalStateException("The solver has been already running.");
        }
        solver = solverFactory.buildSolver();
        completableSolverFuture = managedExecutor.runAsync(() -> {
            solver.addEventListener(event -> {
                if (event.isEveryProblemFactChangeProcessed() && event.getNewBestScore().isSolutionInitialized()) {
                    waitingProblemFactChanges.clear();
                    pinCallAssignedToAgents(event.getNewBestSolution());
                    bestSolutionChangedEventConsumer.accept(event);
                }
            });

            try {
                solver.solve(inputProblem);
            } catch (Throwable throwable) {
                LOGGER.error("Exception during solving.", throwable);
                solving.set(false);
                completableSolverFuture.completeExceptionally(throwable);
                errorHandler.accept(throwable);
            }
        });

        if (!waitingProblemFactChanges.isEmpty()) {
            solver.addProblemFactChanges(new ArrayList<>(waitingProblemFactChanges));
        }
        solving.set(true);
    }

    public synchronized void stopSolving() {
        solving.set(false);
        LOGGER.debug("Stopping a solver.");
        if (completableSolverFuture != null) {
            solver.terminateEarly();
            try {
                completableSolverFuture.get(); // Wait for termination and propagate exceptions.
                completableSolverFuture = null;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Failed to stop solver.", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Failed to stop solver.", e.getCause());
            }
        }
    }

    public boolean isSolving() {
        return solving.get();
    }

    public synchronized void registerProblemFactChange(ProblemFactChange<CallCenter> problemFactChange) {
        LOGGER.debug("Registering a problem fact change (" + problemFactChange + ").");
        waitingProblemFactChanges.add(problemFactChange);
        if (isSolving()) {
            assertSolverIsAlive();
            solver.addProblemFactChange(problemFactChange);
        }
    }

    public synchronized void registerProblemFactChanges(List<ProblemFactChange<CallCenter>> problemFactChanges) {
        LOGGER.debug("Registering multiple (" + problemFactChanges.size() + ") problem fact changes.");
        waitingProblemFactChanges.addAll(problemFactChanges);
        if (isSolving()) {
            assertSolverIsAlive();
            solver.addProblemFactChanges(problemFactChanges);
        }
    }

    private void assertSolverIsAlive() {
        if (completableSolverFuture == null) {
            throw new IllegalStateException("Solver has not been started yet.");
        }
        if (completableSolverFuture.isDone()) {
            try {
                completableSolverFuture.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Solver thread was interrupted.", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Solver thread has died.", e.getCause());
            }
            throw new IllegalStateException("Solver has finished solving even though it operates in daemon mode.");
        }
    }
}
