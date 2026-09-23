package fr.iut_unilim.erp_back.entity;

import fr.iut_unilim.erp_back.converter.StringEncryptionConverter;
import jakarta.persistence.*;

@Entity
@Table(name = "Connection")
public class Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userID")
    private Long id;

    @Column(name = "identifier")
    @Convert(converter = StringEncryptionConverter.class)
    private String identifier;

    @Column(name = "hashedIdentifier", unique = true)
    private String hashedIdentifier;

    @ManyToOne
    @JoinColumn(name = "roleID")
    private Role role;

    @Column(name = "email")
    @Convert(converter = StringEncryptionConverter.class)
    private String email;

    @ManyToOne
    @JoinColumn(name = "universityDepartmentID")
    private UniversityDepartment universityDepartment;

    @JoinColumn(name = "firstName")
    @Convert(converter = StringEncryptionConverter.class)
    private String firstName;

    @JoinColumn(name = "lastName")
    @Convert(converter = StringEncryptionConverter.class)
    private String lastName;

    public Connection() {
    }

    public Connection(String identifier, String hashedIdentifier, Role role, String email, UniversityDepartment universityDepartment) {
        this.identifier = identifier;
        this.hashedIdentifier = hashedIdentifier;
        this.role = role;
        this.email = email;
        this.universityDepartment = universityDepartment;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getHashedIdentifier() {
        return hashedIdentifier;
    }

    public Role getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setHashedIdentifier(String hashedIdentifier) {
        this.hashedIdentifier = hashedIdentifier;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UniversityDepartment getUniversityDepartment() {
        return universityDepartment;
    }

    public void setUniversityDepartment(UniversityDepartment universityDepartment) {
        this.universityDepartment = universityDepartment;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
