package com.pischule.resources;

import com.pischule.model.Link;
import com.pischule.model.Stats;
import com.pischule.services.LinkService;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;

import static jakarta.ws.rs.core.Response.Status.FOUND;

@Path("")
@Blocking
@Produces(MediaType.TEXT_HTML)
public class IndexResource {
    @Inject
    LinkService linkService;

    @GET
    public TemplateInstance get() {
        return Templates.index(linkService.getStats(), null, null);
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@RestForm String url) {
        Link link;
        try {
            link = linkService.saveUrl(url);
        } catch (IllegalArgumentException e) {
            var body = Templates.index(linkService.getStats(), url, e.getMessage());
            return Response.status(400).entity(body).build();
        }

        var uri = URI.create(link.id() + "/view");
        return Response.status(FOUND).location(uri).build();
    }

    @GET
    @Authenticated
    @Path("sign-in")
    public Response singIn(@RestQuery String redirect) {
        return Response.temporaryRedirect(URI.create(redirect)).build();
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance index(Stats stats, String url, String error);
    }
}
