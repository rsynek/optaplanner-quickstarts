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

package org.acme.callcenter.rest.dto;

import java.util.List;
import java.util.stream.Collectors;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Skill;

public class AgentDto {

    public static AgentDto fromAgent(Agent agent) {
        List<CallDto> calls = agent.getAssignedCalls().stream().map(CallDto::fromCall).collect(Collectors.toList());
        return new AgentDto(agent.getId(), agent.getName(), agent.getSkills(), calls);
    }

    private long id;
    private String name;
    private List<Skill> skills;
    private List<CallDto> calls;

    public AgentDto() {
        // Required by Jackson.
    }

    public AgentDto(long id, String name, List<Skill> skills, List<CallDto> calls) {
        this.id = id;
        this.name = name;
        this.skills = skills;
        this.calls = calls;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public List<CallDto> getCalls() {
        return calls;
    }
}
