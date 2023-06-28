package org.metamechanists.quaptics.implementation.blocks.concentrators;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.points.ConnectionPoint;
import org.metamechanists.quaptics.connections.points.ConnectionPointOutput;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.builders.ItemDisplayBuilder;
import org.metamechanists.quaptics.utils.id.ConnectionGroupID;

import java.util.ArrayList;
import java.util.List;

public class SolarConcentrator extends ConnectedBlock {
    private static final float CONNECTION_ADDITIONAL_RADIUS = 0.05F;
    private final float rotationY;
    private final Vector outputLocation = new Vector(0.0F, 0.0F, radius+CONNECTION_ADDITIONAL_RADIUS);
    private final Vector3f mainDisplaySize = new Vector3f(radius*2);
    private final double emissionPower;

    public SolarConcentrator(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe,
                             float radius, float rotationY, double emissionPower, double maxPower) {
        super(group, item, recipeType, recipe, radius, maxPower);
        this.rotationY = rotationY;
        this.emissionPower = emissionPower;
    }

    private ItemDisplay generateMainBlockDisplay(@NotNull Location from) {
        final Vector3f mainDisplayRotation = new Vector3f((float)(Math.PI/2), 0.0F, rotationY);
        return new ItemDisplayBuilder(from.clone().add(RELATIVE_CENTER))
                .setMaterial(Material.GLASS_PANE)
                .setTransformation(Transformations.unadjustedRotateAndScale(mainDisplaySize, mainDisplayRotation))
                .build();
    }

    @Override
    protected void addDisplays(@NotNull DisplayGroup displayGroup, Location location, Player player) {
        displayGroup.addDisplay("main", generateMainBlockDisplay(location));
    }

    @Override
    protected List<ConnectionPoint> generateConnectionPoints(ConnectionGroupID groupID, Player player, Location location) {
        final List<ConnectionPoint> points = new ArrayList<>();
        points.add(new ConnectionPointOutput(groupID, "output", formatPointLocation(player, location, outputLocation)));
        return points;
    }

    @Override
    public void onSlimefunTick(@NotNull Block block, SlimefunItem item, Config data) {
        super.onSlimefunTick(block, item, data);
        final ConnectionGroupID groupID = new ConnectionGroupID(BlockStorage.getLocationInfo(block.getLocation(), Keys.CONNECTION_GROUP_ID));
        final ConnectionGroup group = groupID.get();
        final ConnectionPointOutput output = (ConnectionPointOutput) group.getPoint("output");

        if (!output.hasLink()) {
            return;
        }

        if (block.getWorld().isDayTime()) {
            output.getLink().setPower(emissionPower);
            output.getLink().setEnabled(true);
            return;
        }

        output.getLink().setEnabled(false);
    }

    @Override
    protected @NotNull Material getBaseMaterial() {
        return Material.STRUCTURE_VOID;
    }
}
