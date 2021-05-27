package org.acme.callcenter.persistence.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.persistence.SolutionRepository;

@ApplicationScoped
public class CallCenterSolutionRepository implements SolutionRepository<CallCenter, Long> {

    private static final String LIST_BY_ID_QUERY = "problemId = ?1";

    private final JpaAgentRepository agentRepository;

    private final JpaCallRepository callRepository;

    @Inject
    public CallCenterSolutionRepository(JpaAgentRepository agentRepository, JpaCallRepository callRepository) {
        this.agentRepository = agentRepository;
        this.callRepository = callRepository;
    }

    @Override
    public CallCenter load(Long problemId) {
        List<JpaCall> jpaCalls = callRepository.list(LIST_BY_ID_QUERY, problemId);
        List<JpaAgent> jpaAgents = agentRepository.list(LIST_BY_ID_QUERY, problemId);
        Map<Long, Call> callMap = jpaCalls.stream()
                .collect(Collectors.toMap(JpaCall::getId, this::fromJpa));
        Map<Long, Agent> agentMap = jpaAgents.stream()
                .collect(Collectors.toMap(JpaAgent::getId, this::fromJpa));

        for (JpaAgent jpaAgent : jpaAgents) {
            if (jpaAgent.getNextCall() != null) {
                JpaCall nextJpaCall = jpaAgent.getNextCall();
                long callId = nextJpaCall.getId();
                Call call = callMap.get(callId);
                Agent agent = agentMap.get(jpaAgent.getId());
                agent.setNextCall(call);
                call.setAgent(agent);
                call.setPreviousCallOrAgent(agent);

                while(nextJpaCall != null) {
                    JpaCall currentJpaCall = nextJpaCall;
                    nextJpaCall = nextJpaCall.getNextCall();
                    if (nextJpaCall != null) {
                        Call currentCall = callMap.get(currentJpaCall.getId());
                        Call nextCall = callMap.get(nextJpaCall.getId());
                        currentCall.setNextCall(nextCall);
                        nextCall.setAgent(agent);
                        nextCall.setPreviousCallOrAgent(currentCall);
                    }
                }
            }
        }

        return new CallCenter(agentMap.values(), callMap.values());
    }

    @Override
    public void save(Long problemId, CallCenter callCenter) {

    }

    private Call fromJpa(JpaCall jpaCall) {
        return new Call(jpaCall.getId(), jpaCall.getPhoneNumber(), jpaCall.getRequiredSkills(), jpaCall.getDuration(),
                jpaCall.getStartTime(), jpaCall.getPickUpTime(), jpaCall.isPinned(), jpaCall.getEstimatedWaiting());
    }

    private Agent fromJpa(JpaAgent jpaAgent) {
        return new Agent(jpaAgent.getId(), jpaAgent.getName(), jpaAgent.getSkills());
    }
}
