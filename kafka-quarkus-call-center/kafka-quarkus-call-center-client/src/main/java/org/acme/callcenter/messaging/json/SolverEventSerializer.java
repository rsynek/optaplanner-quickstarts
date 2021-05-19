package org.acme.callcenter.messaging.json;

import org.acme.callcenter.message.AddCallEvent;
import org.acme.callcenter.message.SolverEvent;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class SolverEventSerializer extends ObjectMapperSerializer<SolverEvent> {
}
