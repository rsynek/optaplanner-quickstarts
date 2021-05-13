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

import javax.persistence.Column;
import javax.persistence.Entity;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.PreviousCallOrAgent;
import org.optaplanner.core.api.score.director.ScoreDirector;

@Entity
public class RemoveCallProblemFactChange extends PersistableProblemFactChange {

    @Column
    private long callId;

    RemoveCallProblemFactChange() {
        // Required by JPA.
    }

    public RemoveCallProblemFactChange(long problemId, long callId) {
        super(problemId);
        this.callId = callId;
    }

    @Override
    public void doChange(ScoreDirector<CallCenter> scoreDirector) {
        CallCenter callCenter = scoreDirector.getWorkingSolution();
        callCenter.setLastChangeId(getId());

        Call call = new Call(callId, null);
        Call workingCall = scoreDirector.lookUpWorkingObjectOrReturnNull(call);
        if (workingCall == null) {
            return;
            //throw new IllegalStateException("Working call does not exist (" + callId + ")");
        }

        PreviousCallOrAgent previousCallOrAgent = workingCall.getPreviousCallOrAgent();

        Call nextCall = workingCall.getNextCall();
        if (nextCall != null) {
            scoreDirector.beforeVariableChanged(nextCall, "previousCallOrAgent");
            nextCall.setPreviousCallOrAgent(previousCallOrAgent);
            scoreDirector.afterVariableChanged(nextCall, "previousCallOrAgent");
        }

        scoreDirector.beforeEntityRemoved(workingCall);
        if (!callCenter.getCalls().remove(workingCall)) {
            throw new IllegalStateException("Working solution does not contains the call (" + callId + ").");
        }
        scoreDirector.afterEntityRemoved(workingCall);
        scoreDirector.triggerVariableListeners();
    }

    @Override
    public String toString() {
        return "RemoveCallProblemFactChange{" +
                "callId=" + callId +
                '}';
    }
}
