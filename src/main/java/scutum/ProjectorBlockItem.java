package scutum;

import net.minecraft.block.Block;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ProjectorBlockItem extends BlockItem {
    private final String tooltipKey;

    public ProjectorBlockItem(Block block, Settings settings, String tooltipKey) {
        super(block, settings);
        this.tooltipKey = tooltipKey;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        tooltip.add(Text.translatable(this.tooltipKey));
        Block block = this.getBlock();
        boolean isAdvanced = block == ProjectorMod.ADVANCED_PROJECTOR_BLOCK;
        if (!isAdvanced) tooltip.add(Text.literal("creates a 3x3 forcefield")); else tooltip.add(Text.literal("creates a 6x6 forcefield"));

        int placed = ProjectorStats.estimatePlacedCount(block);
        double tierMultiplier = isAdvanced ? ProjectorBlockEntity.ADVANCED_TIER_MULTIPLIER : ProjectorBlockEntity.BASIC_TIER_MULTIPLIER;
        double energyMultiplier = isAdvanced ? ProjectorBlockEntity.ADVANCED_ENERGY_MULTIPLIER : ProjectorBlockEntity.BASIC_ENERGY_MULTIPLIER;

        int health = (int) Math.ceil(placed * tierMultiplier);
        int energyPerTick = (int) Math.ceil(placed * energyMultiplier);
        if (energyPerTick < 1) energyPerTick = 1;

        tooltip.add(Text.literal(String.format("Health: %d HP", health)));
        tooltip.add(Text.literal(String.format("Energy: %d E/t", energyPerTick)));
        super.appendTooltip(stack, world, tooltip, context);
    }
}