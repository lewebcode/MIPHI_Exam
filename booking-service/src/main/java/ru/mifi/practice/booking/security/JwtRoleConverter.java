package ru.mifi.practice.booking.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JwtRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Object roles = jwt.getClaims().getOrDefault("roles", jwt.getClaims().get("role"));
        if (roles instanceof String roleString) {
            return List.of(new SimpleGrantedAuthority("ROLE_" + roleString));
        }
        if (roles instanceof Collection<?> collection) {
            return collection.stream()
                    .map(Object::toString)
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
