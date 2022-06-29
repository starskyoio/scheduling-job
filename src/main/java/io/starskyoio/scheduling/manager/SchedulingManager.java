package io.starskyoio.scheduling.manager;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.starskyoio.scheduling.entity.SysSchedulingJob;
import io.starskyoio.scheduling.service.SysSchedulingJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author Linus.Lee
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "app-config.scheduling.thread-pool")
public class SchedulingManager implements SchedulingConfigurer, ApplicationContextAware, DisposableBean {
    private static final int JOB_ENABLED = 1;
    private static final int JOB_DISABLED = 0;
    private final SysSchedulingJobService sysSchedulingJobService;
    private final Map<String, ScheduledFuture<?>> scheduledFutureMap = Maps.newConcurrentMap();
    private final Map<String, SysSchedulingJob> jobMap = Maps.newConcurrentMap();
    private ScheduledTaskRegistrar registrar;
    private ApplicationContext applicationContext;

    private int corePoolSize = 10;
    private int maxPoolSize = 10;
    private int keepAliveSeconds = 0;

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    @Override
    public void configureTasks(@NotNull ScheduledTaskRegistrar registrar) {
        registrar.setScheduler(new ConcurrentTaskScheduler(newScheduledExecutor()));
        this.registrar = registrar;
    }


    private ScheduledExecutorService newScheduledExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder().setNameFormat("schedule-pool-%d").build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize, namedThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setCorePoolSize(corePoolSize);
        executor.setMaximumPoolSize(maxPoolSize);
        executor.setKeepAliveTime(keepAliveSeconds, TimeUnit.SECONDS);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return executor;
    }

    public void init() {
        List<SysSchedulingJob> list = sysSchedulingJobService.list();
        if (CollectionUtils.isEmpty(list)) {
            scheduledFutureMap.values().forEach(scheduledFuture -> scheduledFuture.cancel(false));
            scheduledFutureMap.clear();
            jobMap.clear();
            log.info("清空系统定时任务");
            return;
        }
        list.forEach(job -> {
            if (job.getStatus() == JOB_DISABLED) {
                ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(job.getJobKey());
                if (Objects.nonNull(scheduledFuture)) {
                    scheduledFuture.cancel(false);
                    scheduledFutureMap.remove(job.getJobKey());
                    jobMap.remove(job.getJobKey());
                    log.info("删除Job:{}", job);
                }
                return;
            }
            if (job.getStatus() == JOB_ENABLED) {
                if (!CronExpression.isValidExpression(job.getCron())) {
                    log.warn("cron表达式不合法：{}", job);
                    return;
                }
                ScheduledFuture<?> scheduledFuture = scheduledFutureMap.get(job.getJobKey());
                if (Objects.isNull(scheduledFuture)) {
                    Runnable myTask = (Runnable) applicationContext.getBean(job.getJobKey());
                    scheduledFuture = Objects.requireNonNull(registrar.getScheduler())
                            .schedule(myTask, triggerContext -> new CronTrigger(job.getCron()).nextExecutionTime(triggerContext));
                    scheduledFutureMap.put(job.getJobKey(), scheduledFuture);
                    jobMap.put(job.getJobKey(), job);
                    log.info("添加Job:{}", job);
                } else {
                    SysSchedulingJob oldJob = jobMap.get(job.getJobKey());
                    if (Objects.nonNull(oldJob) && !StringUtils.equals(oldJob.getCron(), job.getCron())) {
                        oldJob.setCron(job.getCron());
                        oldJob.setJobName(job.getJobName());
                        oldJob.setJobParams(job.getJobParams());
                        oldJob.setUpdateTime(job.getUpdateTime());
                        log.info("更新Job:{}", job);
                    }
                }
            }
        });
        log.info("加载系统定时任务成功");
    }

    public void refresh() {
        init();
    }

    public List<SysSchedulingJob> getRunningJobs() {
        return ImmutableList.copyOf(jobMap.values());
    }

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void destroy() {
        this.registrar.destroy();
    }
}
