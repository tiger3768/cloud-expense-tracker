package com.aditya.expensetracker.expense_tracker.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

	@Bean(name = AsyncExecutors.EMAIL)
	public Executor emailTaskExecutor() {

	    ThreadPoolTaskExecutor executor =
	            new ThreadPoolTaskExecutor();

	    executor.setCorePoolSize(4);

	    executor.setMaxPoolSize(10);

	    executor.setQueueCapacity(100);

	    executor.setThreadNamePrefix(
	            "email-");

	    executor.setWaitForTasksToCompleteOnShutdown(true);

	    executor.setAwaitTerminationSeconds(30);

	    executor.initialize();

	    return executor;
	}
}