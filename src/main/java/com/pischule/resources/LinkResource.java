package com.pischule.resources;

import com.pischule.services.LinkService;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;

@Path("{id:[A-Za-z0-9_-]{6}}")
@Produces(MediaType.TEXT_HTML)
@Blocking
public class LinkResource {

    @Inject
    Template view;

    @Inject
    Template edit;

    @Inject
    LinkService linkService;

    @Inject
    Template redirect;

    @GET
    public Response redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);
        var uri = linkService.getUrlAndIncrementVisits(id);
        return Response.status(RestResponse.Status.MOVED_PERMANENTLY)
                .location(uri)
                .entity(redirect.data("url", uri))
                .build();
    }

    @GET
    @Path("view")
    public TemplateInstance getView(@RestPath String id) {
        return view.data("link", linkService.getById(id));
    }

    @Authenticated
    @GET
    @Path("edit")
    public TemplateInstance getEdit(@RestPath String id) {
        var link = linkService.getById(id);
        return edit.data("link", link).data("error", null);
    }

    @Authenticated
    @POST
    @Path("edit")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response updateLink(@RestPath String id, @RestForm String url) {
        var link = linkService.getById(id);
        try {
            if (link.redirect().equalsIgnoreCase(url)) {
                throw new IllegalArgumentException("Can't be the same as redirect");
            }
            linkService.updateUrl(id, url);
        } catch (IllegalArgumentException e) {
            var body = edit
                    .data("link", link)
                    .data("error", e.getMessage())
                    .data("url", url);
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
}
