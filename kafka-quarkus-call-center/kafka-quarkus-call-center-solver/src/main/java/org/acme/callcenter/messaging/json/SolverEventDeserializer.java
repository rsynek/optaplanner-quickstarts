package org.acme.callcenter.messaging.json;

import org.acme.callcenter.message.SolverEvent;
import org.acme.callcenter.message.StartSolverEvent;

import io.quarkus.kafka.client.serialization.ObjectMapperDeserializer;

public class SolverEventDeserializer extends ObjectMapperDeserializer<SolverEvent> {

    public SolverEventDeserializer() {
        super(SolverEvent.class);
    }
}
