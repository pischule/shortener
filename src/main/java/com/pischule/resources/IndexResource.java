package com.pischule.resources;

import com.pischule.entity.Link;
import com.pischule.services.LinkService;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

import static javax.ws.rs.core.Response.Status.FOUND;

@Path("")
@Blocking
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
    @Inject
    Template index;

    @Inject
    LinkService linkService;

    @GET
    public TemplateInstance get() {
        return index
                .data("error", null)
                .data("stats", linkService.getStats())
                .data("url", null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@RestForm String url) {
        Link link;
        try {
            link = linkService.saveUrl(url);
        } catch (ConstraintViolationException | URISyntaxException e) {
            var body = index
                    .data("error", "Invalid URL")
                    .data("stats", linkService.getStats())
                    .data("url", url);
            return Response.status(400).entity(body).build();
        }

        var uri = URI.create(link.id + "/view");
        return Response.status(FOUND).location(uri).build();
    }


    @GET
    @Authenticated
    @Path("sign-in")
    public Response singIn(@RestQuery String redirect) {
        return Response.temporaryRedirect(URI.create(redirect)).build();
    }

    @GET
    @Path("sign-out")
    public Response signOut() {
        return Response.seeOther(URI.create("/"))
                .cookie(new NewCookie("q_session", null, "/", null, null, 0, true))
                .build();
    }
}
