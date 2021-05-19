package org.acme.callcenter.rest;

import static org.acme.callcenter.data.DataGenerator.PROBLEM_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Collectors;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.callcenter.InMemoryMessagingConnectorResource;
import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.message.ProlongCallEvent;
import org.acme.callcenter.message.RemoveCallEvent;
import org.acme.callcenter.message.SolverEvent;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;

@QuarkusTest
@QuarkusTestResource(InMemoryMessagingConnectorResource.class)
public class CallResourceTest {

    @Any
    @Inject
    InMemoryConnector connector;

    @Inject
    CallResource callResource;

    private InMemorySink<? extends SolverEvent> solverChannel;

    @BeforeEach
    void setUp() {
        solverChannel = connector.sink(CallCenterChannelNames.SOLVER);
        solverChannel.clear();
    }

    @Test
    void remove_call() {
        final long callId = 42;
        callResource.removeCall(callId);

        List<RemoveCallEvent> receivedEvents = receiveEvents(RemoveCallEvent.class);
        assertThat(receivedEvents).hasSize(1);
        RemoveCallEvent removeCallEvent = receivedEvents.get(0);
        assertThat(removeCallEvent.getProblemId()).isEqualTo(PROBLEM_ID);
        assertThat(removeCallEvent.getCallId()).isEqualTo(callId);
    }

    @Test
    void prolong_call() {
        final long callId = 42;
        callResource.prolongCall(callId);

        List<ProlongCallEvent> receivedEvents = receiveEvents(ProlongCallEvent.class);
        assertThat(receivedEvents).hasSize(1);
        ProlongCallEvent prolongCallEvent = receivedEvents.get(0);
        assertThat(prolongCallEvent.getProblemId()).isEqualTo(PROBLEM_ID);
        assertThat(prolongCallEvent.getCallId()).isEqualTo(callId);
    }

    private <T extends SolverEvent> List<T> receiveEvents(Class<T> type) {
        List<? extends Message<? extends SolverEvent>> receivedEvents = solverChannel.received();
        return receivedEvents.stream().map(message -> type.cast(message.getPayload())).collect(Collectors.toList());
    }
}
