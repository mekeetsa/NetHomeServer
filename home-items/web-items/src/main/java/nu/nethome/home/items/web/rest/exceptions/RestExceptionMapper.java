package nu.nethome.home.items.web.rest.exceptions;

import nu.nethome.home.items.web.rest.exceptions.RestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RestExceptionMapper implements ExceptionMapper<RestException> {

    @Override
    public Response toResponse(RestException exception) {
        return Response.status(exception.httpStatus)
                .entity(exception.getMessage())
                .header(RestException.ERROR_CODE_HEADER, exception.errorCode)
                .header(RestException.ERROR_MESSAGE_HEADER, exception.getMessage())
                .build();
    }
}
