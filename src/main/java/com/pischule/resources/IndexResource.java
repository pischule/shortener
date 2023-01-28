package com.pischule.resources;

import com.pischule.entity.Link;
import com.pischule.services.LinkService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.jboss.resteasy.reactive.RestForm;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status.FOUND;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {
    @Inject
    Template index;

    @Inject
    LinkService linkService;

    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index
                .data("error", null)
                .data("url", "");
    }


    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Blocking
    @Transactional
    public Response post(@RestForm String url) {
        Link link;
        try {
            new URI(url);
            link = linkService.saveUrl(url);
        } catch (ConstraintViolationException e) {
            var violation = e.getConstraintViolations().iterator().next();
            var body = index
                    .data("error", violation.getMessage())
                    .data("url", url);
            return Response.status(400).entity(body).build();
        } catch (URISyntaxException e) {
            var body = index
                    .data("error", "Invalid URL")
                    .data("url", url);
            return Response.status(400).entity(body).build();
        }

        var uri = URI.create("/v/" + link.id);
        return Response.status(FOUND).location(uri).build();
    }

    @Authenticated
    @Path("/login")
    @GET
    public Response login() {
        return Response.seeOther(URI.create("/")).build();
    }
}
