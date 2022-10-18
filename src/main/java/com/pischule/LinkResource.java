package com.pischule;

import com.aventrix.jnanoid.jnanoid.NanoIdUtils;
import io.quarkus.hibernate.reactive.panache.common.runtime.ReactiveTransactional;
import io.quarkus.logging.Log;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

@Path("/")
public class UiResource {

    @Inject
    Template index;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data("link", null);
    }

    @GET
    @Path("/{id}")
    public Uni<Response> redirect(@PathParam("id") String id) {
        Log.info("id");
        return Link.findById(id)
                .onItem().transform(l -> (Link) l)
                .onItem().transform(it -> Response.temporaryRedirect(URI.create(it.url)).build());
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    @ReactiveTransactional
    public Uni<TemplateInstance> post(@FormParam("url") String url) {
        Link link = new Link();
        link.id = NanoIdUtils.randomNanoId().substring(0, 8);
        link.url = url;

        return Link.persist(link)
                .onItem().transform(saved -> index.data("link", link.id));
    }

}
