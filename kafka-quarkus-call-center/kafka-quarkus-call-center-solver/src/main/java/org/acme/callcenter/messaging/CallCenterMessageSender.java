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

package org.acme.callcenter.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.message.BestSolutionEvent;
import org.acme.callcenter.message.CallCenterChannelNames;
import org.acme.callcenter.message.ErrorEvent;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class CallCenterMessageSender {

    @Inject
    @Channel(CallCenterChannelNames.BEST_SOLUTION)
    Emitter<BestSolutionEvent> bestSolutionEmitter;

    @Inject
    @Channel(CallCenterChannelNames.ERROR)
    Emitter<ErrorEvent> eventEmitter;

    public void sendBestSolutionEvent(long problemId) {
        bestSolutionEmitter.send(new BestSolutionEvent(problemId));
    }

    public void sendErrorEvent(long problemId, String exceptionClassName, String exceptionMessage) {
        eventEmitter.send(new ErrorEvent(problemId,exceptionClassName, exceptionMessage));
    }
}
