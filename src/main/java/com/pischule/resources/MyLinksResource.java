package com.pischule.resources;

import com.pischule.services.LinkService;
import io.quarkus.panache.common.Page;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.jboss.resteasy.reactive.RestQuery;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Objects;

@Blocking
@Produces(MediaType.TEXT_HTML)
@Path("my-links")
public class MyLinksResource {
    @Inject
    Template myLinks;

    @Inject
    LinkService linkService;

    @GET
    @Authenticated
    public TemplateInstance get(@RestQuery("page") Integer pageIndex) {
        final var pageSize = 30;
        var panachePage = Page.of(Objects.requireNonNullElse(pageIndex, 0), pageSize);
        var linkList = linkService.getAllOwned(panachePage);

        var template = myLinks;
        if (pageIndex != null) {
            template = myLinks.getFragment("items");
        }

        return template
                .data("nextPage", panachePage.next().index)
                .data("links", linkList);
    }
}
