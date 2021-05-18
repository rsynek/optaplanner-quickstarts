package org.acme.callcenter.rest;

import static org.acme.callcenter.data.DataGenerator.PROBLEM_ID;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.callcenter.InMemoryMessagingConnectorResource;
import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.message.ProlongCallEvent;
import org.acme.callcenter.message.RemoveCallEvent;
import org.eclipse.microprofile.reactive.messaging.Message;
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

    @Test
    void remove_call() {
        final long callId = 42;
        callResource.removeCall(callId);

        InMemorySink<RemoveCallEvent> removeCallChannel = connector.sink(CallCenterChannelNames.REMOVE_CALL);
        List<? extends Message<RemoveCallEvent>> receivedEvents = removeCallChannel.received();
        assertThat(receivedEvents).hasSize(1);
        RemoveCallEvent removeCallEvent = receivedEvents.get(0).getPayload();
        assertThat(removeCallEvent.getProblemId()).isEqualTo(PROBLEM_ID);
        assertThat(removeCallEvent.getCallId()).isEqualTo(callId);
    }

    @Test
    void prolong_call() {
        final long callId = 42;
        callResource.prolongCall(callId);

        InMemorySink<ProlongCallEvent> prolongCallChannel = connector.sink(CallCenterChannelNames.PROLONG_CALL);
        List<? extends Message<ProlongCallEvent>> receivedEvents = prolongCallChannel.received();
        assertThat(receivedEvents).hasSize(1);
        ProlongCallEvent prolongCallEvent = receivedEvents.get(0).getPayload();
        assertThat(prolongCallEvent.getProblemId()).isEqualTo(PROBLEM_ID);
        assertThat(prolongCallEvent.getCallId()).isEqualTo(callId);
    }
}
