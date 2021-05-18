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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.persistence.CallCenterRepository;
import org.acme.callcenter.rest.dto.AgentDto;
import org.acme.callcenter.rest.dto.CallCenterDto;
import org.acme.callcenter.service.SimulationService;
import org.acme.callcenter.messaging.SolverMessageHandler;

import io.quarkus.runtime.StartupEvent;

@Path("/call-center")
public class CallCenterResource {

    @Inject
    SolverMessageHandler solverMessageHandler;

    @Inject
    SimulationService simulationService;

    @Inject
    CallCenterRepository callCenterRepository;

    @Inject
    DataGenerator dataGenerator;

    public void setupDemoData(@Observes StartupEvent startupEvent) {
        CallCenter callCenter = dataGenerator.generateCallCenter();
        callCenterRepository.save(DataGenerator.PROBLEM_ID, callCenter);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CallCenterDto get() {
        Optional<CallCenter> callCenterOptional = callCenterRepository.load(DataGenerator.PROBLEM_ID);
        if (callCenterOptional.isEmpty()) {
            throw new IllegalStateException("No Call Center record found in the repository (" + DataGenerator.PROBLEM_ID + ")");
        }
        CallCenterDto callCenterDto = convert(callCenterOptional.get());
        callCenterDto.setSolving(solverMessageHandler.isSolving());
        return callCenterDto;
    }

    private CallCenterDto convert(CallCenter callCenter) {
        List<AgentDto> agents = callCenter.getAgents().stream().map(AgentDto::fromAgent).collect(Collectors.toList());
        return new CallCenterDto(agents, callCenter.getScore());
    }

    @POST
    @Path("solve")
    public void solve() {
        solverMessageHandler.startSolving(DataGenerator.PROBLEM_ID, callCenter -> {
            simulationService.onNewBestSolution(callCenter);
        });
        simulationService.startSimulation();
    }

    @POST
    @Path("stop")
    public void stop() {
        solverMessageHandler.stopSolving(DataGenerator.PROBLEM_ID);
        simulationService.stopSimulation();
    }
}
