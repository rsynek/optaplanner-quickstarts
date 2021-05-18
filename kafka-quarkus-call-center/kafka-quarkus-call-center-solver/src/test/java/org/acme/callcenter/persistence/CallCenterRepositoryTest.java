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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class CallCenterRepositoryTest {

    @Inject
    CallCenterRepository callCenterRepository;

    @BeforeEach
    @Transactional
    void cleanUp() {
        callCenterRepository.deleteAll();
    }

    @Test
    @TestTransaction
    void save_load_update() {
        Set<Skill> skills = EnumSet.of(Skill.CAR_INSURANCE, Skill.ENGLISH);
        Call call1 = new Call(1L, "123-456-789", skills, Duration.ofSeconds(10));
        Call call2 = new Call(2L, "12-45787-5454", skills, Duration.ofSeconds(10));
        Agent agent = new Agent(1L,"John Smith", EnumSet.of(Skill.CAR_INSURANCE, Skill.ENGLISH));
        Agent agent2 = new Agent(2L,"Beth", EnumSet.of(Skill.LIFE_INSURANCE, Skill.ENGLISH));
        agent.setNextCall(call1);
        call1.setNextCall(call2);
        call1.setAgent(agent);
        call2.setAgent(agent);
        CallCenter initialCallCenter = new CallCenter(Arrays.asList(agent, agent2), Arrays.asList(call1, call2));

        callCenterRepository.save(1L, initialCallCenter);
        CallCenter updatedCallCenter = callCenterRepository.load(1L).get();

        Call call3 = new Call(3L, "123-456-657", skills, Duration.ofSeconds(10));
        call3.setAgent(updatedCallCenter.getAgents().get(0));
        updatedCallCenter.getAgents().get(0).getNextCall().getNextCall().setNextCall(call3);
        updatedCallCenter.getCalls().add(call3);

        Call call4 = new Call(4L, "6565-547-871", skills, Duration.ofSeconds(10));
        call4.setAgent(updatedCallCenter.getAgents().get(1));
        call4.setPreviousCallOrAgent(updatedCallCenter.getAgents().get(1));
        updatedCallCenter.getAgents().get(1).setNextCall(call4);
        updatedCallCenter.getCalls().add(call4);

        callCenterRepository.save(1, updatedCallCenter);

        CallCenter finalCallCenter = callCenterRepository.load(1L).get();
        assertThat(finalCallCenter).usingRecursiveComparison().isEqualTo(updatedCallCenter);
    }
}
