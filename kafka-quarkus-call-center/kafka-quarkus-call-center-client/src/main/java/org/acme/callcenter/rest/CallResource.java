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

package org.acme.callcenter.rest;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.service.SimulationService;
import org.acme.callcenter.service.SolverMessageHandler;

@Path("/call")
public class CallResource {

    @Inject
    SolverMessageHandler solverMessageHandler;

    @Inject
    SimulationService simulationService;

    @DELETE
    @Path("{id}")
    public void deleteCall(@PathParam("id") long id) {
        solverMessageHandler.removeCall(DataGenerator.PROBLEM_ID, id);
    }

    @PUT
    @Path("{id}")
    public void prolongCall(@PathParam("id") long id) {
        solverMessageHandler.prolongCall(DataGenerator.PROBLEM_ID, id);
        simulationService.prolongCall(id);
    }
}
