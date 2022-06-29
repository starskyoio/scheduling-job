package io.starskyoio.scheduling;

import io.starskyoio.scheduling.job.AbstractSchedulingJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(value="myJob2")
public class MyJob2 extends AbstractSchedulingJob {
    @Override
    public void execute() {
        log.info("执行MyJob2成功");

    }

    @Override
    public String getJobKey() {
        return "myJob2";
    }
}
