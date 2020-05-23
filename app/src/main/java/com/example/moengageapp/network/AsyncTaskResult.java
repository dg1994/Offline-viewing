package com.example.moengageapp.network;

public class AsyncTaskResult<T> {
    private T result;
    private Exception exception;

    public AsyncTaskResult(T result) {
        this.result = result;
    }

    public AsyncTaskResult(Exception exception) {
        this.exception = exception;
    }

    public AsyncTaskResult(T result, Exception exception) {
        this.result = result;
        this.exception = exception;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}