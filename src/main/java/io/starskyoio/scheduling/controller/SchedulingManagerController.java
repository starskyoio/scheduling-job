package io.starskyoio.scheduling.controller;

import io.starskyoio.scheduling.common.Result;
import io.starskyoio.scheduling.entity.SysSchedulingJob;
import io.starskyoio.scheduling.manager.SchedulingManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author Linus.Lee
 */
@RestController
@RequestMapping("/schedulingManager/")
@RequiredArgsConstructor
public class SchedulingManagerController {

    private final SchedulingManager schedulingManager;

    @GetMapping("/refresh")
    public Result<?> refresh() {
        schedulingManager.refresh();
        return Result.ok();
    }

    @GetMapping("/jobs")
    public Result<List<SysSchedulingJob>> getRunningJobs() {
        List<SysSchedulingJob> list = schedulingManager.getRunningJobs();
        return Result.ok(list);
    }
}
