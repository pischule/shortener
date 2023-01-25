package com.pischule.resources;

import com.pischule.entity.Link;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Objects;

@ApplicationScoped
@Path("/history")
public class HistoryResource {
    @Inject
    Template history;

    @ConfigProperty(name = "base-url")
    String baseUrl;

    @GET
    @Transactional
    public TemplateInstance get(@RestQuery Integer page) {
        var panachePage = Page.of(Objects.requireNonNullElse(page, 0), 30);
        var links = Link.findAll(Sort.descending("createdAt")).page(panachePage).list();

        var template = history;
        if (page != null) {
            template = history.getFragment("items");
        }

        return template
                .data("baseUrl", baseUrl)
                .data("nextPage", panachePage.next().index)
                .data("links", links);
    }
}
