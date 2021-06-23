/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.schooltimetabling.domain;

import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@Entity
public class Room {

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "distance_matrix",
            joinColumns = {@JoinColumn(name = "room_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "room_id")
    private Map<Room, Integer> distances;

    // No-arg constructor required for Hibernate
    public Room() {
    }

    public Room(String name) {
        this.name = name.trim();
    }

    public Room(long id, String name) {
        this(name);
        this.id = id;
    }

    public int getDistance(Room room) {
        return distances.get(room);
    }

    public void setDistances(Map<Room, Integer> distances) {
        this.distances = distances;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
