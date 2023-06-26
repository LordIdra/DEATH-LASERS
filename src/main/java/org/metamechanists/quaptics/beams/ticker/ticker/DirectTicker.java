package org.metamechanists.quaptics.beams.ticker.ticker;

import lombok.Builder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.joml.Vector3f;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.builders.BlockDisplayBuilder;
import org.metamechanists.quaptics.utils.id.BlockDisplayID;
import org.metamechanists.quaptics.utils.id.TickerID;

@Builder
public class DirectTicker implements DisplayTicker {
    private final BlockDisplayID displayID;

    public DirectTicker(Material material, Location source, Location target, float radius) {
        final Location midpoint = source.clone().add(target).multiply(0.5);
        final Vector3f scale = new Vector3f(radius, radius, (float)(source.distance(target)));
        this.displayID = new BlockDisplayID(new BlockDisplayBuilder(midpoint)
                        .setMaterial(material)
                        .setTransformation(Transformations.lookAlong(scale, Transformations.getDirection(midpoint, target)))
                        .setBrightness(15)
                        .build()
                        .getUniqueId());
    }

    private DirectTicker(TickerID ID) {
        this.displayID = new BlockDisplayID(ID.get());
    }

    public static DirectTicker fromID(TickerID ID) {
        return new DirectTicker(ID);
    }

    public TickerID getID() {
        return new TickerID(displayID.get());
    }

    private BlockDisplay getDisplay() {
        return (BlockDisplay) Bukkit.getEntity(displayID.get());
    }

    @Override
    public void tick() {}

    @Override
    public void remove() {
        getDisplay().remove();
    }

    @Override
    public boolean expired() {
        return true;
    }
}
