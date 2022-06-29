package io.starskyoio.scheduling.job;

public interface SchedulingJob extends Runnable {
    void execute();

    String getJobKey();
}
