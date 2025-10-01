package exam.portal.global_exceptions_handler;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
