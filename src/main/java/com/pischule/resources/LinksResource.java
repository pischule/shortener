package com.pischule.resources;

import com.pischule.services.LinkService;
import io.quarkus.panache.common.Page;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.Blocking;
import org.jboss.resteasy.reactive.RestQuery;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

@Blocking
@Produces(MediaType.TEXT_HTML)
@Path("links")
public class LinksResource {
    @Inject
    Template links;

    @Inject
    LinkService linkService;

    @GET
    @Authenticated
    public TemplateInstance get(@RestQuery("page") Integer pageIndex) {
        final var pageSize = 30;
        var panachePage = Page.of(Objects.requireNonNullElse(pageIndex, 0), pageSize);
        var linkList = linkService.getAllOwned(panachePage);

        var template = links;
        if (pageIndex != null) {
            template = links.getFragment("items");
        }

        return template
                .data("nextPage", panachePage.next().index)
                .data("links", linkList);
    }
}
