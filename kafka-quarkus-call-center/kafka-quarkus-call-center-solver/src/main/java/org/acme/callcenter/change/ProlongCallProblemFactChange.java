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

import java.time.Duration;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.optaplanner.core.api.score.director.ScoreDirector;

@Entity
public class ProlongCallProblemFactChange extends PersistableProblemFactChange {

    @Column
    private long callId;
    @Column
    private Duration prolongation;

    ProlongCallProblemFactChange() {
        // Required by JPA.
    }

    public ProlongCallProblemFactChange(long problemId, long callId, Duration prolongation) {
        super(problemId);
        this.callId = callId;
        this.prolongation = prolongation;
    }

    @Override
    public void doChange(ScoreDirector<CallCenter> scoreDirector) {
        scoreDirector.getWorkingSolution().setLastChangeId(getId());
        Call call = new Call(callId, null);
        Call workingCall = scoreDirector.lookUpWorkingObjectOrReturnNull(call);

        if (workingCall != null) {
            scoreDirector.beforeProblemPropertyChanged(workingCall);
            workingCall.setDuration(workingCall.getDuration().plus(prolongation));
            scoreDirector.afterProblemPropertyChanged(workingCall);
            scoreDirector.triggerVariableListeners();
        }
    }
}
