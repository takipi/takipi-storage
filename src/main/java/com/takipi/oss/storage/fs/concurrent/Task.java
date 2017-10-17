package com.takipi.oss.storage.fs.concurrent;

public interface Task {
	Runnable getRunnable();
}
