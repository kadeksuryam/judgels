package org.iatoki.judgels.jerahmeel.user;

import play.data.validation.Constraints;

import java.util.Arrays;
import java.util.List;

public final class UserAddForm {

    @Constraints.Required
    public String username;

    @Constraints.Required
    public String roles;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public List<String> getRolesAsList() {
        return Arrays.asList(this.roles.split(","));
    }
}
