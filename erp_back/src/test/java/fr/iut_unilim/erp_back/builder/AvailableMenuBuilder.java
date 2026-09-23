package fr.iut_unilim.erp_back.builder;

import fr.iut_unilim.erp_back.dto.MenuResponse;
import fr.iut_unilim.erp_back.entity.AvailableMenu;

import java.util.ArrayList;
import java.util.List;

public class AvailableMenuBuilder {
    private Long id;
    private String label;
    private String permissionKey;
    private String route;
    private String iconId;
    private AvailableMenu parent;
    private List<AvailableMenu> children = new ArrayList<>();

    public AvailableMenuBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public AvailableMenuBuilder label(String label) {
        this.label = label;
        return this;
    }

    public AvailableMenuBuilder permissionKey(String permissionKey) {
        this.permissionKey = permissionKey;
        return this;
    }

    public AvailableMenuBuilder route(String route) {
        this.route = route;
        return this;
    }

    public AvailableMenuBuilder iconId(String iconId) {
        this.iconId = iconId;
        return this;
    }

    public AvailableMenuBuilder parent(AvailableMenu parent) {
        this.parent = parent;
        return this;
    }

    public AvailableMenuBuilder children(List<AvailableMenu> children) {
        this.children = children != null ? children : new ArrayList<>();
        return this;
    }

    public AvailableMenuBuilder addChild(AvailableMenu child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        return this;
    }

    public AvailableMenu build() {
        AvailableMenu menu = new AvailableMenu(label, permissionKey, route, iconId, parent);
        menu.setId(id);
        menu.setChildren(children);
        return menu;
    }

    public static MenuResponse buildDto(AvailableMenu menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getLabel(),
                menu.getRoute(),
                menu.getIconId(),
                menu.getChildren().stream().map(AvailableMenuBuilder::buildDto).toList()
        );
    }
}
