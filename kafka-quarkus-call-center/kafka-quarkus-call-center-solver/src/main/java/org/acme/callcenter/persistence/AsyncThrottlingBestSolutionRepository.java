package org.acme.callcenter.persistence;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.acme.callcenter.service.AsyncBestSolutionRepository;

public class AsyncThrottlingBestSolutionRepository<Solution, ProblemId> implements AsyncBestSolutionRepository<Solution, ProblemId> {

    private AsyncBestSolutionRepository<Solution, ProblemId> bestSolutionRepository;

    private LocalDateTime lastSaveFinished;
    private Duration interval;
    private final AtomicBoolean saveInProgress = new AtomicBoolean(false);

    public AsyncThrottlingBestSolutionRepository(AsyncBestSolutionRepository<Solution, ProblemId> bestSolutionRepository, Duration interval) {
        this.bestSolutionRepository = bestSolutionRepository;
        this.interval = interval;
    }

    @Override
    public Solution load(ProblemId problemId) {
        return bestSolutionRepository.load(problemId);
    }

    @Override
    public CompletableFuture<Boolean> save(ProblemId problemId, Solution solution) {
        if (saveInProgress.compareAndSet(false, true) && isIntervalOver()) {
            return bestSolutionRepository.save(problemId, solution).thenApply(o -> {
                lastSaveFinished = LocalDateTime.now();
                saveInProgress.set(false);
                return true;
            });
        }
        return CompletableFuture.completedFuture(false);
    }



    private boolean isIntervalOver() {
        return LocalDateTime.now().minus(interval).isBefore(lastSaveFinished);
    }
}
