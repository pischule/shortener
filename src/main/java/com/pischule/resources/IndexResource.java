package com.pischule.resources;

import com.pischule.entity.Link;
import com.pischule.util.IdUtil;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.common.annotation.Blocking;
import org.hibernate.validator.constraints.URL;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.FOUND;

@ApplicationScoped
@Path("/")
public class IndexResource {
    @Inject
    Template index;

    @Inject
    IdUtil idUtil;

    @Inject
    Template error;

    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.instance();
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Blocking
    @Transactional
    public Response post(@RestForm @Valid @NotNull @URL String url) {
        var link = new Link();
        link.id = idUtil.generate();
        link.url = url;
        link.persist();

        var uri = URI.create("/v/" + link.id);
        return Response.status(FOUND).location(uri).build();
    }

    @ServerExceptionMapper
    public Response handleValidationError(ValidationException exception) {
        Log.error(exception);
        return Response
                .status(400)
                .entity(error.data("msg", exception.getMessage()).render())
                .header("Content-Type", "text/html")
                .build();
    }
}
