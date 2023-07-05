package org.metamechanists.quaptics.connections;

import lombok.Getter;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display.Brightness;
import org.bukkit.entity.Interaction;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.metamechanists.quaptics.panels.implementation.PointPanel;
import org.metamechanists.quaptics.storage.PersistentDataTraverser;
import org.metamechanists.quaptics.schedulers.PointPanelUpdateScheduler;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.builders.BlockDisplayBuilder;
import org.metamechanists.quaptics.utils.builders.InteractionBuilder;
import org.metamechanists.quaptics.utils.id.simple.BlockDisplayId;
import org.metamechanists.quaptics.utils.id.complex.ConnectionGroupId;
import org.metamechanists.quaptics.utils.id.complex.ConnectionPointId;
import org.metamechanists.quaptics.utils.id.simple.InteractionId;
import org.metamechanists.quaptics.utils.id.complex.LinkId;
import org.metamechanists.quaptics.utils.id.complex.PanelId;

import java.util.Optional;

public class ConnectionPoint {
    private static final float SIZE = 0.1F;
    private static final Vector INTERACTION_OFFSET = new Vector(0, -SIZE/2, 0);
    private static final Color SELECTED_COLOR = Color.fromRGB(0, 255, 0);
    @Getter
    private static final int CONNECTED_BRIGHTNESS = 15;
    @Getter
    private static final int DISCONNECTED_BRIGHTNESS = 3;
    private final ConnectionPointType type;
    private final ConnectionGroupId groupId;
    @Getter
    private final InteractionId interactionId;
    private final BlockDisplayId blockDisplayId;
    private final @Nullable PanelId panelId;
    private @Nullable LinkId linkId;
    @Getter
    private final String name;

    public ConnectionPoint(final ConnectionPointType type, final ConnectionGroupId groupId, final String name,
                              @NotNull final Location location) {
        final Interaction interaction = new InteractionBuilder(location.clone().add(INTERACTION_OFFSET))
                .setWidth(SIZE)
                .setHeight(SIZE)
                .build();
        this.type = type;
        this.groupId = groupId;
        this.interactionId = new InteractionId(interaction.getUniqueId());
        this.blockDisplayId = new BlockDisplayId(new BlockDisplayBuilder(location)
                .setMaterial(type.getMaterial())
                .setTransformation(Transformations.adjustedScale(new Vector3f(SIZE, SIZE, SIZE)))
                .setBrightness(DISCONNECTED_BRIGHTNESS)
                .build()
                .getUniqueId());
        this.panelId = new PointPanel(location, getId()).getId();
        this.name = name;
        saveData();
        updatePanel();
    }

    public ConnectionPoint(final ConnectionPointId pointId) {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(pointId);
        this.type = traverser.getConnectionPointType("type");
        this.groupId = traverser.getConnectionGroupId("groupId");
        this.blockDisplayId = traverser.getBlockDisplayId("blockDisplayId");
        this.interactionId = traverser.getInteractionId("interactionId");
        this.panelId = traverser.getPanelId("panelId");
        this.linkId = traverser.getLinkId("linkId");
        this.name = traverser.getString("name");
    }

    private void saveData() {
        final PersistentDataTraverser traverser = new PersistentDataTraverser(getId());
        traverser.set("type", type);
        traverser.set("groupId", groupId);
        traverser.set("blockDisplayId", blockDisplayId);
        traverser.set("interactionId", interactionId);
        traverser.set("panelId", panelId);
        traverser.set("linkId", linkId);
        traverser.set("name", name);
    }

    public boolean isOutput() {
        return type == ConnectionPointType.OUTPUT;
    }

    public boolean isInput() {
        return type == ConnectionPointType.INPUT;
    }

    public final @NotNull ConnectionPointId getId() {
        return new ConnectionPointId(interactionId);
    }

    public Optional<Link> getLink() {
        return linkId == null ? Optional.empty() : linkId.get();
    }

    public boolean isLinkEnabled() {
        return getLink().isPresent() && getLink().get().isEnabled();
    }

    public Optional<Location> getLocation() {
        return getBlockDisplay().isPresent()
                ? Optional.of(getBlockDisplay().get().getLocation())
                : Optional.empty();
    }

    public Optional<PointPanel> getPointPanel() {
        return panelId == null ? Optional.empty() : Optional.of(new PointPanel(panelId, getId()));
    }

    private Optional<BlockDisplay> getBlockDisplay() {
        return blockDisplayId.get();
    }

    private Optional<Interaction> getInteraction() {
        return interactionId.get();
    }

    public Optional<ConnectionGroup> getGroup() {
        return groupId.get();
    }

    public void remove() {
        getLink().ifPresent(Link::remove);
        getPointPanel().ifPresent(PointPanel::remove);
        getBlockDisplay().ifPresent(BlockDisplay::remove);
        getInteraction().ifPresent(Interaction::remove);
    }

    public void changeLocation(@NotNull final Location location) {
        getBlockDisplay().ifPresent(blockDisplay -> blockDisplay.teleport(location));
        getInteraction().ifPresent(interaction -> interaction.teleport(location.clone().add(INTERACTION_OFFSET)));
        getPointPanel().ifPresent(panel -> panel.changeLocation(location.clone()));
        saveData();
    }

    public void updatePanel() {
        PointPanelUpdateScheduler.scheduleUpdate(panelId, getId());
    }

    public void togglePanelHidden() {
        getPointPanel().ifPresent(PointPanel::toggleHidden);
    }

    public void unlink() {
        this.linkId = null;
        getBlockDisplay().ifPresent(blockDisplay -> blockDisplay.setBrightness(new Brightness(DISCONNECTED_BRIGHTNESS, 0)));
        saveData();
        updatePanel();
    }

    public void link(final LinkId linkId) {
        unlink();
        this.linkId = linkId;
        getBlockDisplay().ifPresent(blockDisplay -> blockDisplay.setBrightness(new Brightness(CONNECTED_BRIGHTNESS, 0)));
        saveData();
        updatePanel();
    }

    public void select() {
        getBlockDisplay().ifPresent(blockDisplay -> {
            blockDisplay.setGlowing(true);
            blockDisplay.setGlowColorOverride(SELECTED_COLOR);
        });
    }

    public void deselect() {
        getBlockDisplay().ifPresent(blockDisplay -> blockDisplay.setGlowing(false));
    }
}
