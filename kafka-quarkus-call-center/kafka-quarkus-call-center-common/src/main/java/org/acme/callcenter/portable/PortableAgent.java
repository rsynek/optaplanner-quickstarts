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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.Skill;

public class PortableAgent {

    public static PortableAgent fromAgent(Agent agent) {
        List<PortableCall> calls = getAssignedCalls(agent);
        return new PortableAgent(agent.getId(), agent.getName(), agent.getSkills(), calls);
    }

    private static List<PortableCall> getAssignedCalls(Agent agent) {
        Call nextCall = agent.getNextCall();
        List<PortableCall> assignedCalls = new ArrayList<>();
        while (nextCall != null) {
            assignedCalls.add(PortableCall.fromCall(nextCall));
            nextCall = nextCall.getNextCall();
        }
        return assignedCalls;
    }

    private long id;
    private String name;
    private Set<Skill> skills;
    private List<PortableCall> calls;

    public PortableAgent() {
        // Required by Jackson.
    }

    public PortableAgent(long id, String name) {
        this.id = id;
        this.name = name;
        this.skills = EnumSet.noneOf(Skill.class);
    }

    public PortableAgent(long id, String name, Set<Skill> skills, List<PortableCall> calls) {
        this.id = id;
        this.name = name;
        this.skills = EnumSet.copyOf(skills);
        this.calls = calls;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public List<PortableCall> getCalls() {
        return calls;
    }
}
