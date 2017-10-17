package com.takipi.oss.storage.fs.concurrent;

import java.util.List;

public interface TaskExecutor {
	void execute(List<Task> tasks);
}
