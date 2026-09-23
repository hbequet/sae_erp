package fr.iut_unilim.erp_back.builder;

import fr.iut_unilim.erp_back.entity.Connection;
import fr.iut_unilim.erp_back.entity.Role;
import fr.iut_unilim.erp_back.entity.UniversityDepartment;
import org.jetbrains.annotations.NotNull;

public class ConnectionBuilder {

    private Long id;
    private String identifier;
    private String hashedIdentifier;
    private Role role;
    private String email;
    private UniversityDepartment universityDepartment;
    private String firstName;
    private String lastName;

    public static ConnectionBuilder aConnection() {
        return new ConnectionBuilder();
    }

    public ConnectionBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public ConnectionBuilder withIdentifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public ConnectionBuilder withHashedIdentifier(String hashedIdentifier) {
        this.hashedIdentifier = hashedIdentifier;
        return this;
    }

    public ConnectionBuilder withRole(Role role) {
        this.role = role;
        return this;
    }

    public ConnectionBuilder withRoleName(String roleName) {
        if (roleName != null) {
            Role r = new Role();
            r.setRoleName(roleName);
            this.role = r;
        } else {
            this.role = null;
        }
        return this;
    }

    public ConnectionBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public ConnectionBuilder withUniversityDepartment(UniversityDepartment universityDepartment) {
        this.universityDepartment = universityDepartment;
        return this;
    }

    public ConnectionBuilder withFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public ConnectionBuilder withLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    @NotNull
    public Connection build() {
        Connection connection = new Connection();
        connection.setId(this.id);
        connection.setIdentifier(this.identifier);
        connection.setHashedIdentifier(this.hashedIdentifier);
        connection.setRole(this.role);
        connection.setEmail(this.email);
        connection.setUniversityDepartment(this.universityDepartment);
        connection.setFirstName(this.firstName);
        connection.setLastName(this.lastName);
        return connection;
    }
}
