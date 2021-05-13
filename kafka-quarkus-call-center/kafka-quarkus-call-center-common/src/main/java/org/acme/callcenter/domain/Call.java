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

import java.time.Duration;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.CustomShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;
import org.optaplanner.core.api.domain.variable.PlanningVariableReference;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Call.class)
@PlanningEntity
public class Call extends PreviousCallOrAgent {

    private String phoneNumber;
    private Set<Skill> requiredSkills;
    private Duration duration = Duration.ZERO;
    private LocalTime startTime;
    private LocalTime pickUpTime;

    @PlanningPin
    private boolean pinned;

    @JsonBackReference
    @PlanningVariable(valueRangeProviderRefs = { "agentRange", "callRange" }, graphType = PlanningVariableGraphType.CHAINED)
    private PreviousCallOrAgent previousCallOrAgent;

    @AnchorShadowVariable(sourceVariableName = "previousCallOrAgent")
    private Agent agent;

    @CustomShadowVariable(variableListenerClass = ResponseTimeUpdatingVariableListener.class,
            sources = { @PlanningVariableReference(variableName = "previousCallOrAgent") })
    private Duration estimatedWaiting;

    public Call() {
        // Required by OptaPlanner.
    }

    public Call(long id, String phoneNumber) {
        super(id);
        this.phoneNumber = phoneNumber;
        this.requiredSkills = EnumSet.noneOf(Skill.class);
        this.startTime = LocalTime.now();
    }

    public Call(long id, String phoneNumber, Set<Skill> requiredSkills, Duration duration) {
        super(id);
        this.phoneNumber = phoneNumber;
        this.requiredSkills = EnumSet.copyOf(requiredSkills);
        this.duration = duration;
        this.startTime = LocalTime.now();
    }

    public Call(long id, String phoneNumber, Skill... requiredSkills) {
        this(id, phoneNumber);
        this.requiredSkills.addAll(Arrays.asList(requiredSkills));
    }

    public Call(long id, String phoneNumber, Set<Skill> requiredSkills, Duration duration, LocalTime startTime,
            LocalTime pickUpTime, boolean pinned, Duration estimatedWaiting) {
        super(id);
        this.phoneNumber = phoneNumber;
        this.requiredSkills = requiredSkills;
        this.duration = duration;
        this.startTime = startTime;
        this.pickUpTime = pickUpTime;
        this.pinned = pinned;
        this.estimatedWaiting = estimatedWaiting;
    }

    @JsonIgnore
    public int getMissingSkillCount() {
        if (agent == null) {
            return 0;
        }

        return (int) requiredSkills.stream()
                .filter(skill -> !agent.getSkills().contains(skill))
                .count();
    }

    @JsonIgnore
    @Override
    public Duration getDurationTillPickUp() {
        Duration durationTillPickUp;
        if (estimatedWaiting == null) {
            return null;
        } else {
            durationTillPickUp = estimatedWaiting.plus(getDuration());
            if (pickUpTime != null) {
                durationTillPickUp = durationTillPickUp.minus(Duration.between(pickUpTime, LocalTime.now()));
            }
        }
        return durationTillPickUp;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public boolean isPinned() {
        return pinned;
    }

    public PreviousCallOrAgent getPreviousCallOrAgent() {
        return previousCallOrAgent;
    }

    public Agent getAgent() {
        return agent;
    }

    public Duration getEstimatedWaiting() {
        return estimatedWaiting;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public void setPreviousCallOrAgent(PreviousCallOrAgent previousCallOrAgent) {
        this.previousCallOrAgent = previousCallOrAgent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public void setEstimatedWaiting(Duration estimatedWaiting) {
        this.estimatedWaiting = estimatedWaiting;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public void setPickUpTime(LocalTime pickUpTime) {
        this.pickUpTime = pickUpTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Call))
            return false;
        Call call = (Call) o;
        return getId().equals(call.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Call{" + getId() + "}";
    }
}
