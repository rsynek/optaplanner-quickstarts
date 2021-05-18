package org.acme.callcenter.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.callcenter.InMemoryMessagingConnectorResource;
import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.message.BestSolutionEvent;
import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.message.StartSolverEvent;
import org.acme.callcenter.persistence.CallCenterRepository;
import org.acme.callcenter.rest.dto.CallCenterDto;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.connectors.InMemorySink;
import io.smallrye.reactive.messaging.connectors.InMemorySource;

@QuarkusTest
@QuarkusTestResource(InMemoryMessagingConnectorResource.class)
public class CallCenterResourceTest {

    private static final long PROBLEM_ID = DataGenerator.PROBLEM_ID;

    @Any
    @Inject
    InMemoryConnector connector;

    @InjectMock
    CallCenterRepository callCenterRepository;

    @Inject
    CallCenterResource callCenterResource;

    @Test
    void start_solving_sends_event() {
        callCenterResource.solve();
        InMemorySink<StartSolverEvent> startSolverChannel = connector.sink(CallCenterChannelNames.START_SOLVER);
        List<? extends Message<StartSolverEvent>> receivedStartSolverEvents = startSolverChannel.received();
        assertThat(receivedStartSolverEvents).hasSize(1);
        StartSolverEvent startSolverEvent = receivedStartSolverEvents.get(0).getPayload();
        assertThat(startSolverEvent.getProblemId()).isEqualTo(PROBLEM_ID);
    }

    @Test
    void best_solution_is_received() {
        CallCenter bestSolution = createBestSolution();
        // The first invocation returns empty call center, the second one the best solution.
        when(callCenterRepository.load(PROBLEM_ID)).thenReturn(Optional.of(CallCenter.emptyCallCenter()), Optional.of(bestSolution));
        assertThat(callCenterResource.get().getAgents()).isEmpty();

        InMemorySource<BestSolutionEvent> bestSolutionChannel = connector.source(CallCenterChannelNames.BEST_SOLUTION);
        BestSolutionEvent bestSolutionEvent = new BestSolutionEvent(PROBLEM_ID);
        bestSolutionChannel.send(bestSolutionEvent);

        verify(callCenterRepository, times(2)).load(eq(PROBLEM_ID));
        CallCenterDto callCenterDto = callCenterResource.get();
        assertThat(callCenterDto.getAgents()).hasSize(1);
        assertThat(callCenterDto.isSolving()).isTrue();
    }

    private CallCenter createBestSolution() {
        Call call = new Call(1L, "465-615-857");
        Agent agent = new Agent(1L, "test agent");

        call.setPreviousCallOrAgent(agent);
        call.setAgent(agent);
        agent.setNextCall(call);
        return new CallCenter(Collections.singletonList(agent), Collections.singletonList(call));
    }
}
