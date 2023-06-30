package org.metamechanists.quaptics.implementation.blocks.manipulators;

import dev.sefiraat.sefilib.entity.display.DisplayGroup;
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.metamechanists.quaptics.QuapticTicker;
import org.metamechanists.quaptics.connections.ConnectionGroup;
import org.metamechanists.quaptics.connections.points.ConnectionPoint;
import org.metamechanists.quaptics.connections.points.ConnectionPointInput;
import org.metamechanists.quaptics.connections.points.ConnectionPointOutput;
import org.metamechanists.quaptics.implementation.base.ConnectedBlock;
import org.metamechanists.quaptics.utils.Keys;
import org.metamechanists.quaptics.utils.Transformations;
import org.metamechanists.quaptics.utils.builders.BlockDisplayBuilder;
import org.metamechanists.quaptics.utils.id.ConnectionGroupID;

import java.util.List;
import java.util.Objects;

public class Capacitor extends ConnectedBlock {
    private static final int CONCRETE_BRIGHTNESS = 15;
    private final Vector3f mainGlassDisplaySize = new Vector3f(settings.getDisplayRadius()*2.0F);
    private final Vector3f tierGlassDisplaySize = new Vector3f(settings.getDisplayRadius()*1.7F);
    private final Vector3f maxConcreteDisplaySize = new Vector3f(settings.getDisplayRadius()*1.65F);
    private final Vector3f displayRotation = new Vector3f((float)(Math.PI/4), (float)(Math.PI/4), 0);
    private final Vector inputPointLocation = new Vector(0.0F, 0.0F, -settings.getConnectionRadius());
    private final Vector outputPointLocation = new Vector(0.0F, 0.0F, settings.getConnectionRadius());

    public Capacitor(ItemGroup group, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe, Settings settings) {
        super(group, item, recipeType, recipe, settings);
    }

    @Override
    protected void addDisplays(@NotNull DisplayGroup displayGroup, @NotNull Location location, Player player) {
        displayGroup.addDisplay("mainGlass", new BlockDisplayBuilder(location.toCenterLocation())
                .setMaterial(Material.GLASS)
                .setTransformation(Transformations.adjustedRotateAndScale(mainGlassDisplaySize, displayRotation))
                .build());
        displayGroup.addDisplay("tierGlass", new BlockDisplayBuilder(location.toCenterLocation())
                .setMaterial(settings.getTier().glassMaterial)
                .setTransformation(Transformations.adjustedRotateAndScale(tierGlassDisplaySize, displayRotation))
                .build());
        displayGroup.addDisplay("concrete", new BlockDisplayBuilder(location.toCenterLocation())
                .setMaterial(settings.getTier().concreteMaterial)
                .setTransformation(Transformations.adjustedRotateAndScale(new Vector3f(), displayRotation))
                .setBrightness(CONCRETE_BRIGHTNESS)
                .setViewRange(VIEW_RANGE_OFF)
                .build());
    }

    @Override
    protected List<ConnectionPoint> generateConnectionPoints(ConnectionGroupID groupID, Player player, Location location) {
        return List.of(
                new ConnectionPointInput(groupID, "input", formatPointLocation(player, location, inputPointLocation)),
                new ConnectionPointOutput(groupID, "output", formatPointLocation(player, location, outputPointLocation)));
    }

    @Override
    public void onQuapticTick(@NotNull ConnectionGroup group) {
        final ConnectionPointInput input = group.getInput("input");
        final ConnectionPointOutput output = group.getOutput("output");
        final String chargeString = BlockStorage.getLocationInfo(group.getLocation(), Keys.CHARGE);
        double charge = chargeString == null ? 0 : Double.parseDouble(chargeString);

        if (input.isLinkEnabled()) {
            final double additionalCharge = input.getLink().getPower() / QuapticTicker.QUAPTIC_TICKS_PER_SECOND;
            charge = settings.stepCharge(charge, additionalCharge);
        }

        if (output.hasLink()) {
            charge = settings.stepCharge(charge, -settings.getEmissionPower());
            if (charge > settings.getEmissionPower()) {
                output.getLink().setAttributes(settings.getEmissionPower(), 0, 0, true);
            } else {
                output.disableLinkIfExists();
            }
        }

        final Display concreteDisplay = getDisplay(group.getLocation(), "concrete");
        if (concreteDisplay != null) {
            concreteDisplay.setTransformationMatrix(
                    Transformations.adjustedRotateAndScale(new Vector3f(maxConcreteDisplaySize)
                            .mul((float)charge/settings.getCapacity()), displayRotation));
        }

        BlockStorage.addBlockInfo(group.getLocation(), Keys.CHARGE, Objects.toString(charge));
    }

    @Override
    public void onInputLinkUpdated(@NotNull ConnectionGroup group) {
        final ConnectionPointInput input = group.getInput("input");
        doBurnoutCheck(group, input);
    }
}
