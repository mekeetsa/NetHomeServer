package nu.nethome.home.items.web.rest.exceptions;

/**
 *
 */
public class RestException extends RuntimeException {
    public static final String ERROR_CODE_HEADER = "ErrorCode";
    public static final String ERROR_MESSAGE_HEADER = "ErrorMessage";
    public final int errorCode;
    public final int httpStatus;

    public RestException(String message, int errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
