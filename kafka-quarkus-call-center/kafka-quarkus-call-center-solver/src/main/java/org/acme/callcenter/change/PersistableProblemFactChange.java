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
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import org.acme.callcenter.domain.CallCenter;
import org.optaplanner.core.api.solver.ProblemFactChange;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class PersistableProblemFactChange implements ProblemFactChange<CallCenter> {

    @Id
    @GeneratedValue
    private long id;

    @Column
    private long problemId;

    protected PersistableProblemFactChange() {
        // Required by JPA.
    }

    public PersistableProblemFactChange(long problemId) {
        this.problemId = problemId;
    }

    public long getId() {
        return id;
    }

    public long getProblemId() {
        return problemId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "id=" + id +
                ", problemId=" + problemId +
                '}';
    }
}
