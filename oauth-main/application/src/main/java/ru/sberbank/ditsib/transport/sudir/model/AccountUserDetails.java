package ru.sberbank.ditsib.transport.sudir.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.sberbank.ditsib.transport.sudir.business.model.Account;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

/**
 * User details of account.
 */
@RequiredArgsConstructor
@Getter
public class AccountUserDetails implements UserDetails {

    private final transient Account account;

    @SuppressWarnings("java:3958")
    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return Optional.ofNullable(account.getRoles()).orElse(new HashMap<>())
                       .keySet().stream()
                       .map(SimpleGrantedAuthority::new)
                       .toList();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return account.getPersonnelNumber();
    }

    @Override
    public @NonNull boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public @NonNull boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public @NonNull boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return account.isActive();
    }

}
