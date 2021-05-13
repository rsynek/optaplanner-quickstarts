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

import java.util.Optional;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.callcenter.domain.CallCenter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CallCenterRepository implements PanacheRepository<CallCenterRecord> {

    @Inject
    ObjectMapper objectMapper;

    @Transactional
    public Optional<CallCenter> load(long problemId) {
        return findByIdOptional(problemId)
                .map(callCenterRecord -> fromJson(callCenterRecord.getCallCenterJson(), problemId));
    }

    @Transactional
    public void save(long problemId, CallCenter callCenter) {
        Optional<CallCenterRecord> callCenterRecord = findByIdOptional(problemId);
        String callCenterJson = toJson(callCenter, problemId);
        if (callCenterRecord.isPresent()) {
            callCenterRecord.get().setCallCenterJson(callCenterJson);
        } else {
            persist(new CallCenterRecord(problemId, callCenterJson));
        }
    }

    public Optional<Long> getFirstActiveId() {
        return find("active = true").firstResultOptional()
                .map(CallCenterRecord::getProblemId);
    }

    @Transactional
    public boolean compareAndSetState(long problemId, boolean expected, boolean setTo) {
        CallCenterRecord callCenterRecord = findById(problemId);
        if (callCenterRecord.isActive() == expected) {
            callCenterRecord.setActive(setTo);
            return true;
        }
        return false;
    }

    private String toJson(CallCenter callCenter, long problemId) {
        try {
            return objectMapper.writeValueAsString(callCenter);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot marshall Call Center with problem ID (" + problemId + ") to JSON.", e);
        }
    }

    private CallCenter fromJson(String callCenterJson, long problemId) {
        try {
            return objectMapper.readValue(callCenterJson, CallCenter.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot unmarshall Call Center with problem ID (" + problemId + ") from JSON.", e);
        }
    }
}
