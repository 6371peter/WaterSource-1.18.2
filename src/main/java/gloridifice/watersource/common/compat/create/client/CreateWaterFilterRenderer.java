package gloridifice.watersource.common.compat.create.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import gloridifice.watersource.common.compat.create.CreateWaterFilterTileEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraftforge.fluids.FluidStack;

public class CreateWaterFilterRenderer extends SafeBlockEntityRenderer<CreateWaterFilterTileEntity> {
    public CreateWaterFilterRenderer(BlockEntityRendererProvider.Context context) {

    }
    @Override
    protected void renderSafe(CreateWaterFilterTileEntity tileEntity, float v, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int i1) {
        SmartFluidTankBehaviour output = tileEntity.outputTank;
        if (output == null) return;
        SmartFluidTankBehaviour.TankSegment outputTank = output.getPrimaryTank();
        FluidStack outputFluidStack = outputTank.getRenderedFluid();
        float outputLevel = outputTank.getFluidLevel().getValue(v);

        float processingPT;
        float max;
        float yOffset;

        if (!outputFluidStack.isEmpty() && outputLevel != 0.0f) {

            boolean top = outputFluidStack.getFluid().getAttributes().isLighterThanAir();
            outputLevel = Math.max(outputLevel, 0.175F);
            processingPT = 0.15625F;
            max = processingPT + 0.6875F;
            yOffset = 0.6875F * outputLevel;
            poseStack.pushPose();
            if (!top) {
                poseStack.translate(0.0, (double) yOffset, 0.0);
            } else {
                poseStack.translate(0.0, (double) (max - processingPT), 0.0);
            }

            FluidRenderer.renderFluidBox(outputFluidStack, processingPT, processingPT - yOffset, processingPT, max, processingPT, max, multiBufferSource, poseStack, i, false);
            poseStack.popPose();
        }
    }
}
