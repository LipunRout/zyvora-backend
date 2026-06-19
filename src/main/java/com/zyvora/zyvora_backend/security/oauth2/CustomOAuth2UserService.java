package com.zyvora.security.oauth2;

import com.zyvora.entity.User;
import com.zyvora.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends OidcUserService {

    private final UserRepository userRepository;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");
        String picture = oidcUser.getAttribute("picture");
        String providerId = oidcUser.getAttribute("sub");

        if (email == null) {
            throw new OAuth2AuthenticationException("Email not found from Google");
        }

        User user = userRepository.findByEmail(email)
                .map(existing -> {
                    existing.setName(name);
                    existing.setPicture(picture);
                    existing.setLastActive(LocalDateTime.now());
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .name(name)
                            .picture(picture)
                            .provider(User.AuthProvider.GOOGLE)
                            .providerId(providerId)
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        // Inject the DB userId into the userInfo claims so it survives into getAttributes()
        Map<String, Object> claims = new HashMap<>(oidcUser.getClaims());
        claims.put("userId", user.getId());
        OidcUserInfo updatedUserInfo = new OidcUserInfo(claims);

        return new DefaultOidcUser(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                oidcUser.getIdToken(),
                updatedUserInfo,
                "email"
        );
    }
}