package com.hekkelman.keylocker.tasks;

public interface TaskCallback<T> {
    void onComplete(TaskResult<T> result);
}
