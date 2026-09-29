package ru.sber.transport.integrations.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE, reason = "Thread was interrupted")
public class ThreadInterruptedException extends RuntimeException {

    public ThreadInterruptedException() {
        super("Thread was interrupted");
    }
}
