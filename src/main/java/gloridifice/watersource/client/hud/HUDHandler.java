package gloridifice.watersource.client.hud;

import gloridifice.watersource.WaterSource;
import gloridifice.watersource.registry.ConfigRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = WaterSource.MODID)
public class HUDHandler {
    private final static WaterFilterStrainerHUD WATER_FILTER_STRAINER_HUD = new WaterFilterStrainerHUD(Minecraft.getInstance());

    @SubscribeEvent(receiveCanceled = true)
    public static void onRenderGameOverlayEvent(RenderGameOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hitResult = mc.hitResult;
        if (!ConfigRegistry.DISABLE_FILTER_STRAINER_HUD.get() && hitResult != null && event.getType() == RenderGameOverlayEvent.ElementType.LAYER) {
            if (!mc.options.hideGui){
                BlockPos pos = hitResult.getType() == HitResult.Type.BLOCK ? ((BlockHitResult) hitResult).getBlockPos() : null;
                if (pos != null) WATER_FILTER_STRAINER_HUD.render(event.getMatrixStack(), pos);
            }
        }
    }
}
