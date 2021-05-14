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

package org.acme.callcenter.persistence;

import javax.persistence.AttributeConverter;

import org.acme.callcenter.domain.CallCenter;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonCallCenterConverter implements AttributeConverter<CallCenter, String> {

    private final static ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(OptaPlannerJacksonModule.createModule());

    @Override
    public String convertToDatabaseColumn(CallCenter attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to convert an object to a JSON attribute.", ex);
        }
    }

    @Override
    public CallCenter convertToEntityAttribute(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, CallCenter.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to convert a JSON attribute to an object.", ex);
        }
    }
}
