package nu.nethome.home.items.web.rest.exceptions;

import nu.nethome.home.item.ExecutionFailure;
import nu.nethome.home.items.web.rest.exceptions.RestException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.net.HttpURLConnection;

@Provider
public class ExecutionFailureMapper implements ExceptionMapper<ExecutionFailure> {

    @Override
    public Response toResponse(ExecutionFailure exception) {
        return Response.status(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .entity(exception.getMessage())
                .header(RestException.ERROR_CODE_HEADER, 200)
                .header(RestException.ERROR_MESSAGE_HEADER, "Execution failure")
                .build();
    }
}
