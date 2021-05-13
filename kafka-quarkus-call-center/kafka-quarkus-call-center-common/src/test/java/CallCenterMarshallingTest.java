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


import org.junit.jupiter.api.BeforeAll;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class CallCenterMarshallingTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(OptaPlannerJacksonModule.createModule());
    }
/*
    @Test
    void marshallingAndUnmarshalling() throws JsonProcessingException {
        Call call = new Call(1L, "123-456-789");
        Agent smith = new Agent(2L,"John Smith");
        call.setPreviousCallOrAgent(smith);
        smith.setNextCall(call);
        call.setAgent(smith);

        Call secondCall = new Call(3L, "12-45787-5454");
        call.setNextCall(secondCall);
        secondCall.setAgent(smith);
        secondCall.setPreviousCallOrAgent(call);

        CallCenter callCenter = new CallCenter(EnumSet.allOf(Skill.class), Collections.singletonList(smith), Arrays.asList(call, secondCall));

        String callCenterJson = objectMapper.writeValueAsString(callCenter);

        CallCenter unmarshalledCallCenter = objectMapper.readValue(callCenterJson, CallCenter.class);
        System.out.println(unmarshalledCallCenter);
    }*/
}
