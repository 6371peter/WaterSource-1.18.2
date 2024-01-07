package gloridifice.watersource.client.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import gloridifice.watersource.WaterSource;
import gloridifice.watersource.common.capability.WaterLevelCapability;
import gloridifice.watersource.registry.CapabilityRegistry;
import gloridifice.watersource.registry.ConfigRegistry;
import gloridifice.watersource.registry.MobEffectRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WaterSource.MODID)
public class WaterLevelHud {
    public static WaterLevelCapability CAPABILITY = null;
    public final static ResourceLocation OVERLAY_BAR = new ResourceLocation(WaterSource.MODID, "textures/gui/hud/water_icons.png");
    public final static ResourceLocation THIRST_BAR = new ResourceLocation(WaterSource.MODID, "textures/gui/hud/thirst_icons.png");
    static Minecraft minecraft = Minecraft.getInstance();

    public static final IIngameOverlay NEW_WATER_LEVEL_HUD = OverlayRegistry.registerOverlayAbove(ForgeIngameGui.FOOD_LEVEL_ELEMENT, "WaterLevel", (gui, poseStack, partialTick, screenWidth, screenHeight) -> {
        boolean isMounted = minecraft.player.getVehicle() instanceof LivingEntity;
        if (
                !ConfigRegistry.DISABLE_ALL_WATER_LEVEL_BAR.get()
                        && !isMounted
                        && !minecraft.options.hideGui
                        && gui.shouldDrawSurvivalElements()
        ) {
            gui.setupOverlayRenderState(true, false);
            render(gui, screenWidth, screenHeight, poseStack);
        }
    });

    public static void register()
    {

    }

    public static void render(ForgeIngameGui gui, int screenWidth, int screenHeight, PoseStack poseStack) {
        minecraft.getProfiler().push("water");

        MobEffectInstance effectInstance = minecraft.player.getEffect(MobEffectRegistry.THIRST.get());

        if (CAPABILITY == null ||minecraft.player.tickCount % 40 == 0) {
            CAPABILITY = minecraft.player.getCapability(CapabilityRegistry.PLAYER_WATER_LEVEL).orElse(null);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        if (effectInstance == null) {
            RenderSystem.setShaderTexture(0, OVERLAY_BAR);
        } else { RenderSystem.setShaderTexture(0, THIRST_BAR); }

        // TODO:Add Client Config to change this
        int left = screenWidth / 2 + 91;
        int top = screenHeight - gui.right_height;
        gui.right_height += 10;

        int level = CAPABILITY.getWaterLevel();
        int saturation = CAPABILITY.getWaterSaturationLevel();

        for (int i = 0; i < 10; ++i) {
            int idx = i * 2 + 1;
            int x = left - i * 8 - 9;

            GuiComponent.blit(poseStack, x, top, 0, 0, 9, 9, 42, 9);
            if (idx < level)
                GuiComponent.blit(poseStack, x, top, 16, 0, 9, 9, 42, 9);
            else if (idx == level)
                GuiComponent.blit(poseStack, x, top, 8, 0, 9, 9, 42, 9);
            if (ConfigRegistry.OPEN_WATER_SATURATION_LEVEL.get()) {
                if (idx < saturation) {
                    GuiComponent.blit(poseStack, x, top - 1, 25, 0, 9, 9, 42, 9);
                    GuiComponent.blit(poseStack, x, top + 1, 33, 0, 9, 9, 42, 9);
                }
                else if (idx == saturation)
                    GuiComponent.blit(poseStack, x, top + 1, 33, 0, 9, 9, 42, 9);
            }
        }

        RenderSystem.disableBlend();
        RenderSystem.setShaderTexture(0, GuiComponent.GUI_ICONS_LOCATION);
        minecraft.getProfiler().pop();
    }
}
