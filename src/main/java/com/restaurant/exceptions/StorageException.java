package com.restaurant.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class StorageException extends BaseException {


    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageException(Throwable cause) {
        super(cause);
    }
}
