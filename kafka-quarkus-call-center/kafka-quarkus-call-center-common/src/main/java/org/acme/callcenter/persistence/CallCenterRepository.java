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
import javax.persistence.LockModeType;
import javax.transaction.Transactional;

import org.acme.callcenter.domain.CallCenter;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class CallCenterRepository implements PanacheRepository<CallCenterRecord> {

    @Transactional
    public Optional<CallCenter> load(long problemId) {
        return findByIdOptional(problemId).map(CallCenterRecord::getCallCenter);
    }

    @Transactional
    public void save(long problemId, CallCenter callCenter) {
        Optional<CallCenterRecord> callCenterRecord = findByIdOptional(problemId);
        if (callCenterRecord.isPresent()) {
            callCenterRecord.get().setCallCenter(callCenter);
        } else {
            persist(new CallCenterRecord(problemId, callCenter));
        }
    }

    @Transactional
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
}
