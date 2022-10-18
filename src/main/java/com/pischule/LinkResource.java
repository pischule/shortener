package com.pischule;

import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;

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
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data("link", null);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    @Path("/{id}")
    public Uni<Response> redirect(@PathParam("id") String id) {
        return Link.findById(id)
                .onItem().transform(l -> (Link) l)
                .onItem().ifNotNull().transform(link -> link.url)
                .onItem().ifNull().continueWith("/error/404")
                .onItem().transform(url -> Response.temporaryRedirect(URI.create(url)).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @ReactiveTransactional
    public Uni<TemplateInstance> post(@Context UriInfo uriInfo, @FormParam("url") String url) {
        Link link = new Link();
        link.url = url;
        return Link.persist(link)
                .onItem().transform(saved -> view.data("link", link.id).data("requestUrl", uriInfo.getRequestUri()));
    }

}
