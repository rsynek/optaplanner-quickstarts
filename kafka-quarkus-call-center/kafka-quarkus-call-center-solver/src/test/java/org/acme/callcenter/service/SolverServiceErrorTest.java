package org.acme.callcenter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.callcenter.InMemoryMessagingConnectorResource;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.message.ErrorEvent;
import org.acme.callcenter.persistence.CallCenterRepository;
import org.acme.callcenter.persistence.ProblemFactChangeRepository;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;

@QuarkusTest
@QuarkusTestResource(InMemoryMessagingConnectorResource.class)
public class SolverServiceErrorTest {

    private static final long PROBLEM_ID = 1L;

    @Any
    @Inject
    InMemoryConnector connector;

    @Inject
    CallCenterRepository callCenterRepository;

    @Inject
    ProblemFactChangeRepository problemFactChangeRepository;

    @Inject
    SolverService solverService;

    @BeforeEach
    @Transactional
    void cleanUp() {
        callCenterRepository.deleteAll();
        problemFactChangeRepository.deleteAll();
    }

    @Test
    void solver_exception_is_sent() {
        // Save input problem to a DB.
        Agent agent = new Agent(1L, "Bob");
        CallCenter inputProblem = new CallCenter(Collections.singletonList(agent), Collections.emptyList());
        callCenterRepository.save(PROBLEM_ID, inputProblem);

        // Start solving. The solver should throw an exception due to duplicate @PlanningId.
        solverService.startSolving(PROBLEM_ID);
        final long callId = 1L;
        Call call = new Call(callId, "126-498-784");
        Call duplicateCall = new Call(callId, "657-787-246");
        solverService.addCall(PROBLEM_ID, call);
        solverService.addCall(PROBLEM_ID, duplicateCall);

        InMemorySink<ErrorEvent> errorChannel = connector.sink(CallCenterChannelNames.ERROR);
        await()
                .timeout(Duration.ofSeconds(5))
                .until(() -> !errorChannel.received().isEmpty());

        List<? extends Message<ErrorEvent>> receivedEvents = errorChannel.received();
        assertThat(receivedEvents).hasSize(1);
        ErrorEvent errorEvent = receivedEvents.get(0).getPayload();
        assertThat(errorEvent.getProblemId()).isEqualTo(PROBLEM_ID);
        assertThat(errorEvent.getExceptionClassName()).isEqualTo(IllegalStateException.class.getName());
        assertThat(errorEvent.getExceptionMessage()).contains("have the same planningId");
    }
}
