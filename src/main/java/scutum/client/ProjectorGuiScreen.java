package scutum.client;

import scutum.ProjectorMod;
import scutum.ProjectorScreenHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.network.PacketByteBufs;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.PacketByteBuf;

@Environment(EnvType.CLIENT)
public class ProjectorGuiScreen extends HandledScreen<ProjectorScreenHandler> {
    private final int BAR_WIDTH = 120;
    private ButtonWidget showAreaButton;
    private ButtonWidget applyButton;
    private boolean showing = false;

    public ProjectorGuiScreen(ProjectorScreenHandler handler, net.minecraft.entity.player.PlayerInventory inv, Text title) { super(handler, inv, title); }

    @Override protected void init() {
        super.init(); int cx = this.width/2; int cy = this.height/2;
        showAreaButton = this.addDrawableChild(new ButtonWidget(cx - BAR_WIDTH/2, cy - 18, BAR_WIDTH, 20, Text.of("Show area: Off"), btn -> { showing = !showing; btn.setMessage(Text.of("Show area: " + (showing ? "On" : "Off"))); PacketByteBuf buf = PacketByteBufs.create(); BlockPos pos = this.handler.getPos(); buf.writeBlockPos(pos); buf.writeBoolean(showing); ClientPlayNetworking.send(ProjectorMod.TOGGLE_PREVIEW_PACKET, buf); }));
        applyButton = this.addDrawableChild(new ButtonWidget(cx - BAR_WIDTH/2, cy + 8, BAR_WIDTH, 20, Text.of("\u2713 Apply Field"), btn -> { PacketByteBuf buf = PacketByteBufs.create(); buf.writeBlockPos(this.handler.getPos()); ClientPlayNetworking.send(ProjectorMod.APPLY_FIELD_PACKET, buf); this.client.player.closeHandledScreen(); }));
    }

    @Override public void render(MatrixStack ms, int mouseX, int mouseY, float delta) {
        this.renderBackground(ms); super.render(ms, mouseX, mouseY, delta);
        drawCenteredText(ms, this.textRenderer, this.title, this.x + this.backgroundWidth/2, this.y + 6, 0xFFFFFF);
        int energyPercent = this.handler.getPropertyDelegate().get(0);
        int healthPercent = this.handler.getPropertyDelegate().get(1);
        int energyCostPerTick = this.handler.getPropertyDelegate().get(2);
        int rawHP = this.handler.getPropertyDelegate().get(3);
        int maxHP = this.handler.getPropertyDelegate().get(4);
        int cx = this.width/2; int bx = cx - BAR_WIDTH/2; int by = this.height/2 - 60;
        fill(ms, bx, by, bx + BAR_WIDTH, by + 8, 0xFF222222); int energyFill = Math.max(0, Math.min(BAR_WIDTH, energyPercent * BAR_WIDTH / 100)); fill(ms, bx, by, bx + energyFill, by + 8, 0xFF00AAFF); drawCenteredText(ms, this.textRenderer, Text.of(String.format("Energy: %d%%", energyPercent)), cx, by - 10, 0xFFFFFF);
        int hy = by + 18; fill(ms, bx, hy, bx + BAR_WIDTH, hy + 8, 0xFF222222); int healthFill = Math.max(0, Math.min(BAR_WIDTH, healthPercent * BAR_WIDTH / 100)); fill(ms, bx, hy, bx + healthFill, hy + 8, 0xFFFF5555); drawCenteredText(ms, this.textRenderer, Text.of(String.format("Field Health: %d/%d (%d%%)", rawHP, maxHP, healthPercent)), cx, hy - 10, 0xFFFFFF);
        int drainT = energyCostPerTick; int drainS = energyCostPerTick * 20; drawCenteredText(ms, this.textRenderer, Text.of(String.format("Drain: %d E/t (%d E/s)", drainT, drainS)), cx, hy + 24, 0xFFFFFF);
    }
}