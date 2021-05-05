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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;

import org.acme.callcenter.DataGenerator;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.core.api.solver.SolverFactory;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class SolverServiceTest {

    @Inject
    ManagedExecutor managedExecutor;

    @Inject
    SolverFactory<CallCenter> solverFactory;

    @Inject
    DataGenerator dataGenerator;

    private SolverService solverService;

    private AtomicReference<Throwable> errorDuringSolving = new AtomicReference<>();
    private AtomicReference<CallCenter> bestSolution = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        solverService = new SolverService(solverFactory, managedExecutor);
    }

    @Test
    @Timeout(60)
    void restart_solver_between_changes_multiple_threads() throws InterruptedException {
        CallCenter callCenter = dataGenerator.generateCallCenter();
        callCenter.getCalls().add(dataGenerator.generateCall(15));
        bestSolution.set(callCenter);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        BlockingQueue<Call> addCalls = new LinkedBlockingQueue<>();
        BlockingQueue<Call> removeCalls = new LinkedBlockingQueue<>();
        for (int i = 0; i < 100; i++) {
            addCalls.put(dataGenerator.generateCall(10));
        }

        int callCount = addCalls.size();
        AtomicInteger addCount = new AtomicInteger(callCount);
        AtomicInteger removeCount = new AtomicInteger(callCount);

        CountDownLatch allChangesProcessed = new CountDownLatch(1);
        restartSolving(addCount, removeCount, allChangesProcessed);

        // keep adding calls
        Runnable addCallRunnable = () -> {
            while (addCount.decrementAndGet() >= 0) {
                try {
                    Call call = addCalls.take();
                    solverService.addCall(call);
                    removeCalls.put(call);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();

                }
            }
        };

        Runnable removeCallRunnable = () -> {
            while (removeCount.decrementAndGet() >= 0) {
                try {
                    Call call = removeCalls.take();
                    solverService.removeCall(call.getId());
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Runnable restartSolverRunnable = () -> {
            final int count = 5;
            int i = 1;
            while (i <= count) {
                if (addCount.get() < callCount / i) {
                    restartSolving(addCount, removeCount, allChangesProcessed);
                    i++;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        executorService.submit(restartSolverRunnable);

        executorService.submit(addCallRunnable);
        executorService.submit(addCallRunnable);
        executorService.submit(addCallRunnable);

        executorService.submit(removeCallRunnable);
        executorService.submit(removeCallRunnable);
        executorService.submit(removeCallRunnable);

        executorService.submit(() -> {
            for (int i = 0; i < 10; i++) {
                solverService.addCall(dataGenerator.generateCall(100));
            }
        });

        allChangesProcessed.await();
        if (errorDuringSolving.get() != null) {
            Assertions.fail("Exception during solving", errorDuringSolving.get());
        }
        assertThat(bestSolution.get().getCalls()).hasSize(11);
    }

    private void restartSolving(AtomicInteger addCount, AtomicInteger removeCount, CountDownLatch allChangesProcessed) {
        if (solverService.isSolving()) {
            solverService.stopSolving();
        }
        solverService.startSolving(bestSolution.get(), (bestSolutionChangedEvent) -> {
            if (bestSolutionChangedEvent.isEveryProblemFactChangeProcessed()) {
                bestSolution.set(bestSolutionChangedEvent.getNewBestSolution());
                if (addCount.get() <= 0 && removeCount.get() <= 0) {
                    allChangesProcessed.countDown();
                }
            }
        }, throwable -> {
            throwable.printStackTrace();
            errorDuringSolving.set(throwable);
        });
    }
}
