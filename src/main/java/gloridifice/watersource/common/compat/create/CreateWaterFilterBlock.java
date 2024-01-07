package gloridifice.watersource.common.compat.create;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import gloridifice.watersource.data.ModItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.*;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class CreateWaterFilterBlock extends Block implements IWrenchable, IBE<CreateWaterFilterTileEntity> {
    public CreateWaterFilterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter blockGetter, @NotNull BlockPos blockPos, @NotNull CollisionContext collisionContext) {
        return AllShapes.SPOUT;
    }

    @Override
    public void setPlacedBy(@NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pState, LivingEntity pPlacer, @NotNull ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state,level,pos,newState);
    }

    @Override
    public Class<CreateWaterFilterTileEntity> getBlockEntityClass() {
        return CreateWaterFilterTileEntity.class;
    }

    @Override
    public BlockEntityType<? extends CreateWaterFilterTileEntity> getBlockEntityType() {
        return CreateRegistry.CREATE_WATER_FILTER_TE.get();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        else {
            boolean flag = false;
            CreateWaterFilterTileEntity tileEntity = (CreateWaterFilterTileEntity) level.getBlockEntity(pos);
            ItemStackHandler strainerHandler = tileEntity.strainer.orElse(new ItemStackHandler());
            ItemStack strainerStack = strainerHandler.getStackInSlot(0);
            ItemStack heldItem = player.getItemInHand(interactionHand);
            if (!heldItem.isEmpty()) {
                System.out.println(ForgeRegistries.ITEMS.tags().getTag(ModItemTags.STRAINERS));
                if (ForgeRegistries.ITEMS.tags().getTag(ModItemTags.STRAINERS).contains(heldItem.getItem())) {
                    if (strainerStack.isEmpty()) {
                        strainerHandler.insertItem(0, heldItem.copy(), true);
                        if (!player.isCreative()) {
                            ItemStack itemStack1 = heldItem.copy();
                            itemStack1.setCount(heldItem.getCount() - 1);
                            player.setItemInHand(interactionHand, itemStack1);
                        } else {
                            ItemStack heldItem1 = heldItem.copy();
                            player.setItemInHand(interactionHand, strainerStack);
                            strainerHandler.setStackInSlot(0, heldItem1);
                        }
                        flag = true;
                    }
                }
            } else if (player.getPose() == Pose.CROUCHING) {
                if (tileEntity.inputTank.isEmpty()) {
                    if (!strainerStack.isEmpty()) {
                        if (!player.getInventory().add(strainerStack)) {
                            player.drop(strainerStack, false);
                        }
                        strainerHandler.setStackInSlot(0, ItemStack.EMPTY);
                    }
                    flag = true;
                }
            }
            return flag ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
    }
}
