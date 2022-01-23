package me.lizardofoz.drgflares.client;

import com.mojang.blaze3d.systems.RenderSystem;
import me.lizardofoz.drgflares.DRGFlareRegistry;
import me.lizardofoz.drgflares.config.PlayerSettings;
import me.lizardofoz.drgflares.config.ServerSettings;
import me.lizardofoz.drgflares.util.DRGFlarePlayerAspect;
import me.lizardofoz.drgflares.util.DRGFlaresUtil;
import me.lizardofoz.drgflares.util.FlareColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

@Environment(EnvType.CLIENT)
public class FlareHUDRenderer
{
    private static final Identifier HUD_TEXTURE = new Identifier("drg_flares", "textures/gui/hud.png");
    private static final MinecraftClient client = MinecraftClient.getInstance();

    public static void render(MatrixStack matrixStack, float tickDelta)
    {
        if (!ServerSettings.CURRENT.regeneratingFlaresEnabled.value || client.player == null || client.player.isSpectator())
            return;

        int widgetX = (int) (client.getWindow().getScaledWidth() * PlayerSettings.INSTANCE.flareUISlotX.value);
        int widgetY = (int) (client.getWindow().getScaledHeight() * PlayerSettings.INSTANCE.flareUISlotY.value) - 19;
        Text keyHintLabel = PlayerSettings.INSTANCE.throwFlareKey.getBoundKeyLocalizedText();
        boolean shouldRenderKeybindHint = PlayerSettings.INSTANCE.flareButtonHint.value && keyHintLabel.asString().length() == 1;
        FlareColor flareColor = FlareColor.RandomColorPicker.unwrapRandom(PlayerSettings.INSTANCE.flareColor.value, false);
        ItemStack flareDisplayStack = new ItemStack(DRGFlareRegistry.getInstance().getFlareItemTypes().get(flareColor));
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();

        //Frame
        RenderSystem.setShaderTexture(0, HUD_TEXTURE);
        RenderSystem.enableBlend();
        DrawableHelper.drawTexture(matrixStack, widgetX - 3, widgetY - 3, -200, 0, 0, 22, 22, 32, 32);  //Frame
        if (shouldRenderKeybindHint)
            DrawableHelper.drawTexture(matrixStack, widgetX + 12, widgetY - 6, -200, 22, 0, 10, 10, 32, 32); //Keybind hint bcg

        float zOffset = client.getItemRenderer().zOffset;
        client.getItemRenderer().zOffset = -170;
        client.getItemRenderer().renderInGuiWithOverrides(flareDisplayStack, widgetX, widgetY);
        client.getItemRenderer().zOffset = zOffset;

        if (!DRGFlaresUtil.hasUnlimitedRegeneratingFlares(client.player))
        {
            //Progress Bar
            int count = DRGFlarePlayerAspect.clientLocal.getFlaresLeft();
            int currentRegenStatus = DRGFlarePlayerAspect.clientLocal.getFlareRegenStatus();
            int regenBarMaxValue = ServerSettings.CURRENT.regeneratingFlaresRechargeTime.value * 20;
            if (count < ServerSettings.CURRENT.regeneratingFlaresMaxCharges.value)
            {
                RenderSystem.disableTexture();
                float h = Math.max(0.0F, currentRegenStatus / (float) regenBarMaxValue);
                int i = Math.round(currentRegenStatus * 12.0F / regenBarMaxValue);
                int j = MathHelper.hsvToRgb(h / 3, 1, 1);
                renderGuiQuad(bufferBuilder, widgetX + 1, widgetY + 2, 2, 13, 0, 0, 0, 0);
                renderGuiQuad(bufferBuilder, widgetX + 1, widgetY + 14 - i, 1, i, 111, j >> 16 & 255, j >> 8 & 255, j & 255);
                RenderSystem.enableTexture();
            }

            //Amount Text
            String countText = String.valueOf(count);
            VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(bufferBuilder);
            client.textRenderer.draw(countText, (float) (widgetX + 19 - 2 - client.textRenderer.getWidth(countText)), (float) (widgetY + 6 + 3), 16777215, true, matrixStack.peek().getPositionMatrix(), provider, false, 0, 15728880);
            provider.draw();
        }

        //Keybind Hint Text
        if (shouldRenderKeybindHint)
        {
            matrixStack.push();
            matrixStack.scale(0.7f, 0.7f, 0.7f);
            VertexConsumerProvider.Immediate provider = VertexConsumerProvider.immediate(bufferBuilder);
            client.textRenderer.draw(keyHintLabel, (float) (widgetX + 15) / 0.7f, (float) (widgetY - 4) / 0.7f, 16777215, true, matrixStack.peek().getPositionMatrix(), provider, false, 0, 15728880);
            provider.draw();
            matrixStack.pop();
        }
    }


    private static void renderGuiQuad(BufferBuilder buffer, int x, int y, int width, int height, int z, int red, int green, int blue)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(x, y, z).color(red, green, blue, 255).next();
        buffer.vertex(x, y + height, z).color(red, green, blue, 255).next();
        buffer.vertex(x + width, y + height, z).color(red, green, blue, 255).next();
        buffer.vertex(x + width, y, z).color(red, green, blue, 255).next();
        Tessellator.getInstance().draw();
    }
}