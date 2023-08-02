package com.pischule.resources;

import com.pischule.model.Link;
import com.pischule.services.LinkService;
import io.quarkus.logging.Log;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import java.net.URI;

@Path("{id:[A-Za-z0-9_-]{6}}")
@Produces(MediaType.TEXT_HTML)
@Blocking
public class LinkResource {

    @Inject
    LinkService linkService;

    @GET
    public Response redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);
        var uri = linkService.incrementVisitsAndGetUri(id);
        var body = Templates.redirect(uri);
        return Response.status(RestResponse.Status.MOVED_PERMANENTLY)
                .location(uri)
                .entity(body)
                .build();
    }

    @GET
    @Path("view")
    public TemplateInstance getView(@RestPath String id) {
        var link = linkService.getById(id);
        return Templates.view(link);
    }

    @Authenticated
    @GET
    @Path("edit")
    public TemplateInstance getEdit(@RestPath String id) {
        var link = linkService.getById(id);
        return Templates.edit(link.id(), link.url(), null);
    }

    @Authenticated
    @POST
    @Path("edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateLink(@RestPath String id, @RestForm String url) {
        var link = linkService.getById(id);
        try {
            linkService.updateUrl(link, url);
        } catch (IllegalArgumentException e) {
            var body = Templates.edit(link.id(), url, e.getMessage());
            return Response.status(400).entity(body).build();
        }

        return Response.seeOther(URI.create(id + "/view")).build();
    }

    @Authenticated
    @POST
    @Path("delete")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response deleteLink(@RestPath String id) {
        linkService.delete(id);
        return Response.seeOther(URI.create("/my-links")).build();
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance view(Link link);

        public static native TemplateInstance edit(String id, String url, String error);

        public static native TemplateInstance redirect(URI uri);
    }
}
