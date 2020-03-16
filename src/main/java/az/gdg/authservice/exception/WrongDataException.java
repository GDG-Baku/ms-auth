package az.gdg.authservice.exception;

public class WrongDataException extends RuntimeException {

    public WrongDataException(String message) {
        super(message);
    }
}
