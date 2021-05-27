package org.acme.callcenter.persistence;

import java.util.concurrent.CompletableFuture;

public interface SolutionRepository<Solution, ProblemId> {

    Solution load(ProblemId problemId);

    void save(ProblemId problemId, Solution solution);
}
