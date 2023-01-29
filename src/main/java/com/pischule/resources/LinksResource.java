package com.pischule.resources;

import com.pischule.entity.Link;
import io.quarkus.oidc.IdToken;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.Authenticated;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Objects;

@ApplicationScoped
@Authenticated
@Path("/links")
public class LinksResource {
    @Inject
    Template links;

    @ConfigProperty(name = "base-url")
    String baseUrl;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @GET
    @Transactional
    public TemplateInstance get(@RestQuery Integer page) {
        var panachePage = Page.of(Objects.requireNonNullElse(page, 0), 30);
        var creator = idToken.getSubject();
        var linkList = Link.find("creator", Sort.descending("createdAt"), creator).page(panachePage).list();

        var template = links;
        if (page != null) {
            template = links.getFragment("items");
        }

        return template
                .data("baseUrl", baseUrl)
                .data("loggedIn", idToken.getSubject())
                .data("nextPage", panachePage.next().index)
                .data("links", linkList);
    }
}
