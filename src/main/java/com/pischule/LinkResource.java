package com.pischule;

import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

@Path("/")
public class LinkResource {

    @Inject
    Template index;

    @Inject
    Template view;

    @GET
    @Path("")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.instance();
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ReactiveTransactional
    public Uni<Response> post(@RestForm String url) {
        Link link = new Link();
        link.url = url;
        return Link.persist(link)
                .onItem().transform(v -> Response.seeOther(URI.create("/v/" + link.id)).build());
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/v/{id:[A-Za-z0-9_-]{8}}")
    public Uni<TemplateInstance> view(@Context UriInfo uriInfo, @RestPath String id) {
        return Link.findById(id)
                .onItem().transform(l -> (Link) l)
                .onItem().transform(link -> view
                        .data("link", link.id)
                        .data("visits", link.visits)
                        .data("absoluteLink", uriInfo.getBaseUri() + link.id));
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id:[A-Za-z0-9_-]{8}}")
    @ReactiveTransactional
    public Uni<Response> redirect(@RestPath String id) {
        Log.infof("redirect, id=%s", id);
        return Link.findById(id)
                .onItem().transform(l -> (Link) l)
                .onItem().ifNotNull().invoke(l -> l.visits++)
                .onItem().transform(link -> link.url)
                .onItem().transform(url -> Response.temporaryRedirect(URI.create(url)).build());
    }
}
