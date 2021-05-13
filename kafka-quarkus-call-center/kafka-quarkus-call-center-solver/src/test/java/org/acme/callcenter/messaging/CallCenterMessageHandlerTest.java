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

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.callcenter.message.StopSolverEvent;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySource;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class CallCenterMessageHandlerTest {

    @Any
    @Inject
    InMemoryConnector connector;

    @Test
    void stopSolver() {
        InMemorySource<StopSolverEvent> stopSolverChannel = connector.source("stop_solver");
        StopSolverEvent solverCommand = new StopSolverEvent(1L);
        stopSolverChannel.send(solverCommand);
/*
        await().<List<? extends Message<Beverage>>>until(queue::received, t -> t.size() == 1);

        Beverage queuedBeverage = queue.received().get(0).getPayload();
        Assertions.assertEquals(Beverage.State.READY, queuedBeverage.getPreparationState());
        Assertions.assertEquals("coffee", queuedBeverage.getBeverage());
        Assertions.assertEquals("Coffee lover", queuedBeverage.getCustomer());
        Assertions.assertEquals("1234", queuedBeverage.getOrderId());*/
    }
/*
    @Test
    void startSolving() throws InterruptedException {
        InMemorySource<StartSolverEvent> startSolverChannel = connector.source("start_solver");
        StartSolverEvent startSolverEvent = new StartSolverEvent(1L, dataGenerator.generateCallCenter());
        startSolverChannel.send(startSolverEvent);

        InMemorySource<AddCallEvent> addCallChannel = connector.source("add_call");
        PlanningCall call = new PlanningCall(1L,"123-456-789", EnumSet.of(Skill.CAR_INSURANCE, Skill.ENGLISH), 10);
        AddCallEvent addCallEvent = new AddCallEvent(1L, call);
        addCallChannel.send(addCallEvent);


        Thread.sleep(10_000L);
    }*/
}
