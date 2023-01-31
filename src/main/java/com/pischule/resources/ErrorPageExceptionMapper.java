package com.pischule.resources;

import io.quarkus.logging.Log;
import io.quarkus.qute.Template;

import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ErrorPageExceptionMapper implements ExceptionMapper<Exception> {

    @Inject
    Template error;

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof WebApplicationException webApplicationException) {
            Response originalErrorResponse = webApplicationException.getResponse();
            return renderError(originalErrorResponse.getStatus(), webApplicationException.getMessage());
        } else if (e instanceof IllegalArgumentException illegalArgumentException) {
            return renderError(400, illegalArgumentException.getMessage());
        } else {
            Log.error("Uncaught exception", e);
            return renderError(500, "Internal Server Error");
        }
    }

    private Response renderError(int code, String message) {
        var body = error.data("code", code)
                .data("message", message)
                .render();
        return Response.status(code)
                .header("Content-Type", MediaType.TEXT_HTML)
                .entity(body)
                .build();
    }
}
