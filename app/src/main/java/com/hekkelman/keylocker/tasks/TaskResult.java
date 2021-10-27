package com.hekkelman.keylocker.tasks;

public abstract class TaskResult<T> {
    private TaskResult() {}

    public static final class Success<T> extends TaskResult<T> {
        public T data;

        public Success(T data) {
            this.data = data;
        }
    }

    public static final class Error<T> extends TaskResult<T> {
        public Exception exception;

        public Error(Exception exception) {
            this.exception = exception;
        }
    }
}
