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

package org.acme.callcenter.change;

import javax.persistence.Convert;
import javax.persistence.Entity;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.persistence.JsonCallConverter;
import org.optaplanner.core.api.score.director.ScoreDirector;

@Entity
public class AddCallProblemFactChange extends PersistableProblemFactChange {

    @Convert(converter = JsonCallConverter.class)
    private Call call;

    AddCallProblemFactChange() {
        // Required by JPA.
    }

    public AddCallProblemFactChange(long problemId, Call call) {
        super(problemId);
        this.call = call;
    }

    @Override
    public void doChange(ScoreDirector<CallCenter> scoreDirector) {
        CallCenter callCenter = scoreDirector.getWorkingSolution();
        callCenter.setLastChangeId(getId());

        scoreDirector.beforeEntityAdded(call);
        callCenter.getCalls().add(call);
        scoreDirector.afterEntityAdded(call);
        scoreDirector.triggerVariableListeners();
    }

    public Call getCall() {
        return call;
    }

    @Override
    public String toString() {
        String callId = call == null ? "null" : call.getId().toString();
        return "AddCallProblemFactChange{" +
                "callId=" + callId +
                '}';
    }
}
