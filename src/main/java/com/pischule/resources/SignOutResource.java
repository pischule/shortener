package com.pischule.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;

import java.net.URI;

@Path("sign-out")
public class SignOutResource {
    @GET
    public Response get() {
        var sessionCookie = new NewCookie.Builder("q_session")
                .maxAge(0)
                .build();
        return Response.seeOther(URI.create("/"))
                .cookie(sessionCookie)
                .build();
    }
}
