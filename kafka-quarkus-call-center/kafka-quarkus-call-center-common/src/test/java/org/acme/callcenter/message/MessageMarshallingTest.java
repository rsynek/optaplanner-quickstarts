package org.acme.callcenter.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.acme.callcenter.domain.Call;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class MessageMarshallingTest {

    private static final long PROBLEM_ID = 1L;

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void marshall_unmarshall_startSolverEvent() {
        StartSolverEvent startSolverEvent = new StartSolverEvent(PROBLEM_ID);
        assertMarshallUnmarshall(startSolverEvent, StartSolverEvent.class);
    }

    @Test
    void marshall_unmarshall_stopSolverEvent() {
        StopSolverEvent stopSolverEvent = new StopSolverEvent(PROBLEM_ID);
        assertMarshallUnmarshall(stopSolverEvent, StopSolverEvent.class);
    }

    @Test
    void marshall_unmarshall_addCallEvent() {
        Call call = new Call(1L, "5465-554-157");
        AddCallEvent addCallEvent = new AddCallEvent(PROBLEM_ID, call);
        assertMarshallUnmarshall(addCallEvent, AddCallEvent.class);
    }

    @Test
    void marshall_unmarshall_removeCallEvent() {
        RemoveCallEvent removeCallEvent = new RemoveCallEvent(PROBLEM_ID, 1L);
        assertMarshallUnmarshall(removeCallEvent, RemoveCallEvent.class);
    }

    @Test
    void marshall_unmarshall_prolongCallEvent() {
        ProlongCallEvent prolongCallEvent = new ProlongCallEvent(PROBLEM_ID, 1L, Duration.ofSeconds(20));
        assertMarshallUnmarshall(prolongCallEvent, ProlongCallEvent.class);
    }

    private <T extends SolverEvent> void assertMarshallUnmarshall(T solverEvent, Class<T> eventType) {
        T unmarshalledEvent = null;
        try {
            unmarshalledEvent = objectMapper.readValue(objectMapper.writeValueAsString(solverEvent), eventType);
        } catch (JsonProcessingException jsonProcessingException) {
            Assertions.fail("Exception was thrown during event (un)marshalling.", jsonProcessingException);
        }
        assertThat(unmarshalledEvent).usingRecursiveComparison().isEqualTo(solverEvent);
    }
}
