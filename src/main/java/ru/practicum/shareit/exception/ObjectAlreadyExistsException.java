package ru.practicum.shareit.exception;

public class ObjectAlreadyExistsException extends RuntimeException {
    public ObjectAlreadyExistsException() {
        super("Такой объект уже существует");
    }

    public ObjectAlreadyExistsException(String fieldName) {
        super(String.format("Объект с таким значением '%s' уже существует", fieldName));
    }

    public ObjectAlreadyExistsException(String fieldName, String value) {
        super(String.format("Объект с таким значением '%s'(%s) уже существует", fieldName, value));
    }
}
