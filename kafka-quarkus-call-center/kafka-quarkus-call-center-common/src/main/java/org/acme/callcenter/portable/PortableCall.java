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

package org.acme.callcenter.portable;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Set;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.Skill;

public class PortableCall {

    public static PortableCall fromCall(Call call) {
        return new PortableCall(call.getId(), call.getPhoneNumber(), call.getRequiredSkills(), call.getDuration(),
                call.getStartTime(), call.getPickUpTime(), call.isPinned(), call.getEstimatedWaiting());
    }

    private long id;
    private String phoneNumber;
    private Set<Skill> requiredSkills;
    private Duration duration;
    private LocalTime startTime;
    private LocalTime pickUpTime;
    private boolean pinned;
    private Duration estimatedWaiting;

    public PortableCall(long id, String phoneNumber, Set<Skill> requiredSkills, Duration duration, LocalTime startTime,
            LocalTime pickUpTime, boolean pinned, Duration estimatedWaiting) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.requiredSkills = requiredSkills;
        this.duration = duration;
        this.startTime = startTime;
        this.pickUpTime = pickUpTime;
        this.pinned = pinned;
        this.estimatedWaiting = estimatedWaiting;
    }

    public long getId() {
        return id;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Set<Skill> getRequiredSkills() {
        return requiredSkills;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getPickUpTime() {
        return pickUpTime;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Duration getEstimatedWaiting() {
        return estimatedWaiting;
    }

}
