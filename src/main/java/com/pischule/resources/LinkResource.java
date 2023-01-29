package com.pischule.resources;

import com.pischule.entity.Link;
import com.pischule.services.LinkService;
import io.quarkus.oidc.IdToken;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.NoSuchElementException;

@Produces(MediaType.TEXT_HTML)
@Path("/l")
@Blocking
public class LinkResource {

    @Inject
    Template view;

    @Inject
    Template edit;

    @Inject
    Template error;

    @Inject
    LinkService linkService;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @ConfigProperty(name = "base-url")
    String baseUrl;

    @GET
    @Path("/{id}")
    public TemplateInstance getLinkPage(@RestPath String id) {
        Link l = (Link) Link.findByIdOptional(id).orElseThrow();
        return view.data("link", l)
                .data("isCreator", isCreator(l))
                .data("loggedIn", idToken.getSubject())
                .data("baseUrl", baseUrl);
    }

    @GET
    @Authenticated
    @Path("/{id}/edit")
    public TemplateInstance getEditPage(@RestPath String id) {
        Link l = (Link) Link.findByIdOptional(id).orElseThrow();
        if (!isCreator(l)) {
            throw new NotAllowedException("");
        }

        return edit.data("link", l)
                .data("error", null)
                .data("loggedIn", idToken.getSubject())
                .data("baseUrl", baseUrl);
    }

    @DELETE
    @Path("/{id}")
    @Authenticated
    @Transactional
    public Response deleteLink(@RestPath String id) {
        Link l = (Link) Link.findByIdOptional(id).orElseThrow();
        if (!isCreator(l)) {
            throw new NotAllowedException("");
        }

        l.delete();
        return Response.seeOther(URI.create(".")).build();
    }

    @POST
    @Path("/{id}")
    @Authenticated
    @Transactional
    public Response editLink(@RestPath String id, @RestForm String url) {
        Link l = (Link) Link.findByIdOptional(id).orElseThrow();
        if (!isCreator(l)) {
            throw new NotAllowedException("");
        }

        try {
            linkService.updateUrl(l, url);
        } catch (Exception e) {
            var body = edit
                    .data("link", l)
                    .data("loggedIn", idToken.getSubject())
                    .data("error", "Invalid URL")
                    .data("baseUrl", baseUrl)
                    .data("url", url);
            return Response.status(400).entity(body).build();
        }

        return Response.seeOther(URI.create("/l/" + id)).build();
    }

    @ServerExceptionMapper(NoSuchElementException.class)
    Response handleNotFound(NoSuchElementException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error
                        .data("error", 404)
                        .data("baseUrl", baseUrl)
                        .data("description", "Link not found")
                        .data("loggedIn", idToken.getSubject())
                        .render())
                .build();
    }

    @ServerExceptionMapper(NotAllowedException.class)
    Response handleUnauthorized(NotAllowedException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error
                        .data("error", 403)
                        .data("baseUrl", baseUrl)
                        .data("description", "You have not permissions to edit this link")
                        .data("loggedIn", idToken.getSubject())
                        .render())
                .build();
    }

    private boolean isCreator(Link link) {
        return idToken.getSubject() != null && idToken.getSubject().equals(link.creator);
    }
}
