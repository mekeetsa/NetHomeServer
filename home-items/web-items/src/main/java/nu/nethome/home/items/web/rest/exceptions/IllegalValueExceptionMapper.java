package nu.nethome.home.items.web.rest.exceptions;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.item.IllegalValueException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.HttpURLConnection;

@Provider
public class IllegalValueExceptionMapper implements ExceptionMapper<IllegalValueException> {

    @Override
    public Response toResponse(IllegalValueException exception) {
        return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .entity(exception.getMessage())
                .header(RestException.ERROR_CODE_HEADER, 300)
                .header(RestException.ERROR_MESSAGE_HEADER, "Illegal Value")
                .build();
    }
}
