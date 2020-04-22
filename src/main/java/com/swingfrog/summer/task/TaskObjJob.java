package com.swingfrog.summer.task;

import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;

public class TaskObjJob implements Job{

	private static final Map<Trigger, TaskJob> triggerMap = new HashMap<>();
	
	public static void bindTriggerWithObj(Trigger trigger, TaskJob taskJob) {
		triggerMap.put(trigger, taskJob);
	}
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		TaskJob taskJob = triggerMap.get(context.getTrigger());
		if (taskJob != null) {
			taskJob.execute();
		}
	}
}
