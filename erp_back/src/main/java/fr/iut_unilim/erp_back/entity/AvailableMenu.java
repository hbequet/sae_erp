package fr.iut_unilim.erp_back.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "AvailableMenu")
public class AvailableMenu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "menuID")
    private Long id;

    @Column(name = "menuLabel")
    private String label;

    @Column(name = "permissionKey")
    private String permissionKey;

    @Column(name = "route")
    private String route;

    @Column(name = "iconID")
    private String iconId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentID")
    private AvailableMenu parent;

    @OneToMany(mappedBy = "parent")
    private List<AvailableMenu> children;

    public AvailableMenu() {
    }

    public AvailableMenu(String label, String permissionKey, String route, String iconId, AvailableMenu parent) {
        this.label = label;
        this.permissionKey = permissionKey;
        this.route = route;
        this.iconId = iconId;
        this.parent = parent;
    }

    public String getPermissionKey() {
        return permissionKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public String getIconId() {
        return iconId;
    }

    public String getRoute() {
        return route;
    }

    public AvailableMenu getParent() {
        return parent;
    }

    public void setParent(AvailableMenu parent) {
        this.parent = parent;
    }

    public List<AvailableMenu> getChildren() {
        return children;
    }

    public void setChildren(List<AvailableMenu> children) {
        this.children = (children != null) ? children : new ArrayList<>();
    }
}
