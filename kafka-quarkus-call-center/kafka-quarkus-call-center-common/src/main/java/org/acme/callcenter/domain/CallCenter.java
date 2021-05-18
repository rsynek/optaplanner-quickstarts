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

package org.acme.callcenter.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@PlanningSolution
public class CallCenter {

    public static CallCenter emptyCallCenter() {
        return new CallCenter();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long lastChangeId;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "agentRange")
    private List<Agent> agents = new ArrayList<>();

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "callRange")
    private List<Call> calls = new ArrayList<>();

    @PlanningScore
    private HardSoftScore score;

    public CallCenter() {
        // Required by OptaPlanner.
    }

    public CallCenter(Collection<Agent> agents, Collection<Call> calls) {
        this.agents = new ArrayList<>(agents);
        this.calls = new ArrayList<>(calls);
    }

    @ProblemFactCollectionProperty
    public Set<Skill> getSkills() {
        return EnumSet.allOf(Skill.class);
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Call> getCalls() {
        return calls;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public Long getLastChangeId() {
        return lastChangeId;
    }

    public void setLastChangeId(long lastChangeId) {
        this.lastChangeId = lastChangeId;
    }
}
