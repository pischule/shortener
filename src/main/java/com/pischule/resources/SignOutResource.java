package com.pischule.resources;

import io.quarkus.oidc.OidcSession;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("sign-out")
public class SignOutResource {
    @Inject
    OidcSession oidcSession;

    @GET
    public Response get() {
        oidcSession.logout().await().indefinitely();
        return Response.seeOther(URI.create("/"))
                .build();
    }
}
