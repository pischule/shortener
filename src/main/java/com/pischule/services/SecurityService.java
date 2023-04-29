package com.pischule.services;

import io.quarkus.oidc.IdToken;
import io.quarkus.oidc.UserInfo;
import io.smallrye.common.constraint.Nullable;
import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Optional;

@ApplicationScoped
@Named("securityService")
public class SecurityService {
    @Inject
    UserInfo userInfo;

    @Inject
    @IdToken
    JsonWebToken idToken;

    @Nullable
    public String getUserId() {
        return Optional.ofNullable(idToken.getSubject())
                .or(() -> Optional.ofNullable(userInfo)
                        .filter(it -> it.getJsonObject() != null)
                        .map(it -> it.getLong("id"))
                        .map(it -> "github-" + it)).orElse(null);
    }

    public String getUserName() {
        return Optional.ofNullable(userInfo)
                .filter(it -> userInfo.getJsonObject() != null)
                .map(it -> it.getString("name"))
                .orElse("User");
    }
}
