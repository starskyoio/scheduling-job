package io.starskyoio.scheduling;

import io.starskyoio.scheduling.job.AbstractSchedulingJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value = "myJob")
public class MyJob extends AbstractSchedulingJob {
    @Override
    public void execute() {
        log.info("执行MyJob成功");
        int a = 10/0;
    }

    @Override
    public String getJobKey() {
        return "myJob";
    }
}
