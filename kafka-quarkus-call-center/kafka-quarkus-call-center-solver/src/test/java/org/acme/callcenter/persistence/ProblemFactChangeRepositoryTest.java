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
import java.time.LocalTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import javax.inject.Inject;

import org.acme.callcenter.change.AddCallProblemFactChange;
import org.acme.callcenter.change.PersistableProblemFactChange;
import org.acme.callcenter.change.ProlongCallProblemFactChange;
import org.acme.callcenter.change.RemoveCallProblemFactChange;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.Skill;
import org.junit.jupiter.api.Test;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class ProblemFactChangeRepositoryTest {

    private static final long PROBLEM_ID = 1L;

    @Inject
    ProblemFactChangeRepository problemFactChangeRepository;

    @Test
    @TestTransaction
    void save_deleteByIdLesserThanOrEqualTo() {
        Call call = new Call(1L, "456-789-123");
        AddCallProblemFactChange addCallPfc = new AddCallProblemFactChange(PROBLEM_ID, call);
        ProlongCallProblemFactChange prolongCallPfc = new ProlongCallProblemFactChange(PROBLEM_ID, 1L, Duration.ofSeconds(10L));
        RemoveCallProblemFactChange removeCallPfc = new RemoveCallProblemFactChange(PROBLEM_ID, 1L);

        problemFactChangeRepository.save(addCallPfc);
        problemFactChangeRepository.save(prolongCallPfc);
        problemFactChangeRepository.save(removeCallPfc);

        List<PersistableProblemFactChange> persistableProblemFactChanges = problemFactChangeRepository.listAll();
        assertThat(persistableProblemFactChanges).hasSize(3);

        PersistableProblemFactChange changeWithHighestId = persistableProblemFactChanges.stream()
                .max(Comparator.comparing(PersistableProblemFactChange::getId)).get();
        long lastChangeId = changeWithHighestId.getId() - 1;
        problemFactChangeRepository.deleteByIdLesserThanOrEqualTo(lastChangeId);

        persistableProblemFactChanges = problemFactChangeRepository.listAll();
        assertThat(persistableProblemFactChanges).hasSize(1);
        assertThat(persistableProblemFactChanges.get(0).getId()).isGreaterThan(lastChangeId);
    }

    @Test
    @TestTransaction
    void persist_addCallProblemFactChange() {
        Call call = new Call(1L, "456-789-123", EnumSet.of(Skill.ENGLISH, Skill.LIFE_INSURANCE), Duration.ofSeconds(10),
                LocalTime.now(), LocalTime.now().plusSeconds(20), false, Duration.ofSeconds(120));

        AddCallProblemFactChange pfcToPersist = new AddCallProblemFactChange(PROBLEM_ID, call);
        problemFactChangeRepository.save(pfcToPersist);

        PersistableProblemFactChange foundPfc = problemFactChangeRepository.listAll().get(0);
        assertThat(foundPfc).usingRecursiveComparison().isEqualTo(pfcToPersist);
    }
}
