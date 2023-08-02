package com.pischule.resources;

import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErrorPageExceptionMapper implements ExceptionMapper<Exception> {

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
        var body = Templates.error(code, message);
        return Response.status(code)
                .header("Content-Type", MediaType.TEXT_HTML)
                .entity(body)
                .build();
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance error(int code, String message);
    }
}
