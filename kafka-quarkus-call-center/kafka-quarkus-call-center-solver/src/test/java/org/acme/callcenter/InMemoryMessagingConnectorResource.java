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

package org.acme.callcenter;

import java.util.HashMap;
import java.util.Map;

import org.acme.callcenter.message.CallCenterChannelNames;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.reactive.messaging.connectors.InMemoryConnector;

public class InMemoryMessagingConnectorResource implements QuarkusTestResourceLifecycleManager {

    @Override
    public Map<String, String> start() {
        Map<String, String> env = new HashMap<>();
        // incoming channels
        Map<String, String> startSolverChannelProps = InMemoryConnector.switchIncomingChannelsToInMemory(CallCenterChannelNames.START_SOLVER);
        Map<String, String> stopSolverChannelProps = InMemoryConnector.switchIncomingChannelsToInMemory(CallCenterChannelNames.STOP_SOLVER);
        Map<String, String> addCallChannelProps = InMemoryConnector.switchIncomingChannelsToInMemory(CallCenterChannelNames.ADD_CALL);
        Map<String, String> removeCallChannelProps = InMemoryConnector.switchIncomingChannelsToInMemory(CallCenterChannelNames.REMOVE_CALL);
        Map<String, String> prolongCallChannelProps = InMemoryConnector.switchIncomingChannelsToInMemory(CallCenterChannelNames.PROLONG_CALL);
        // outgoing channels
        Map<String, String> bestSolutionChannelProps = InMemoryConnector.switchOutgoingChannelsToInMemory(CallCenterChannelNames.BEST_SOLUTION);
        Map<String, String> errorChannelProps = InMemoryConnector.switchOutgoingChannelsToInMemory(CallCenterChannelNames.ERROR);

        env.putAll(startSolverChannelProps);
        env.putAll(stopSolverChannelProps);
        env.putAll(addCallChannelProps);
        env.putAll(removeCallChannelProps);
        env.putAll(prolongCallChannelProps);
        env.putAll(bestSolutionChannelProps);
        env.putAll(errorChannelProps);
        return env;
    }

    @Override
    public void stop() {
        InMemoryConnector.clear();
    }
}
