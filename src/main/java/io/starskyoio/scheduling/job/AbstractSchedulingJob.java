package io.starskyoio.scheduling.job;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Maps;
import io.starskyoio.scheduling.entity.SysSchedulingLog;
import io.starskyoio.scheduling.service.SysSchedulingLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Linus.Lee
 */
@Slf4j
public abstract class AbstractSchedulingJob implements SchedulingJob, ApplicationContextAware {
    private static final int SUCCESS = 0;
    private static final int FAIL = 1;
   private ApplicationContext applicationContext;
   
    @Override
    public void run() {
        SysSchedulingLogService logService = applicationContext.getBean(SysSchedulingLogService.class);
        SysSchedulingLog sysLog = new SysSchedulingLog();
        sysLog.setJobKey(getJobKey());
        sysLog.setCreateTime(LocalDateTime.now());
        Map<String, Object> data = Maps.newHashMap();
        try {
            this.execute();
            data.put("msg", "系统定时任务执行成功");
            sysLog.setStatus(SUCCESS);
        } catch (Throwable ex) {
            data.put("msg", "系统定时任务执行失败");
            data.put("ex", ExceptionUtils.getStackTrace(ex));
            sysLog.setJobKey(getJobKey());
            sysLog.setStatus(FAIL);
        } finally {
            sysLog.setData(JSON.toJSONString(data));
            logService.save(sysLog);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
