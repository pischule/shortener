package com.pischule;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/error")
public class ErrorResource {
    @Inject
    Template error;

    @GET
    @Path("/{description}")
    public TemplateInstance get(@PathParam("description") String description) {
        return error.data("title", "Error")
                .data("description", description);
    }
}
