package org.acme.callcenter.service;

import java.util.concurrent.CompletableFuture;

public interface AsyncBestSolutionRepository<Solution, ProblemId> {

    Solution load(ProblemId problemId);

    CompletableFuture<?> save(ProblemId problemId, Solution solution);
}
