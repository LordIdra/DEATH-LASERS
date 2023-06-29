package org.metamechanists.quaptics.utils.builders;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.joml.Matrix4f;

public class BlockDisplayBuilder {
    private final Location location;
    private Material material;
    private BlockData blockData;
    private Matrix4f transformation;
    private Color glowColor;
    private Integer brightness;
    private Float viewRange;

    public BlockDisplayBuilder(Location location) {
        this.location = location;
    }
    public BlockDisplay build() {
        return location.getWorld().spawn(location, BlockDisplay.class, display -> {
            if (material != null) {
                display.setBlock(material.createBlockData());
            }
            if (blockData != null) {
                display.setBlock(blockData);
            }
            if (transformation != null) {
                display.setTransformationMatrix(transformation);
            }
            if (glowColor != null) {
                display.setGlowing(true);
                display.setGlowColorOverride(glowColor);
            }
            if (brightness != null) {
                display.setBrightness(new Display.Brightness(brightness, 0));
            }
            if (viewRange != null) {
                display.setViewRange(viewRange);
            }
            display.setDisplayWidth(0);
            display.setDisplayHeight(0);
        });
    }

    public BlockDisplayBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }
    public BlockDisplayBuilder setBlockData(BlockData blockData) {
        // Overrides material
        this.blockData = blockData;
        return this;
    }
    public BlockDisplayBuilder setTransformation(Matrix4f transformation) {
        this.transformation = transformation;
        return this;
    }
    public BlockDisplayBuilder setBrightness(int brightness) {
        this.brightness = brightness;
        return this;
    }
    public BlockDisplayBuilder setGlow(Color glowColor) {
        this.glowColor = glowColor;
        return this;
    }
    public BlockDisplayBuilder setViewRange(float viewRange) {
        this.viewRange = viewRange;
        return this;
    }
}