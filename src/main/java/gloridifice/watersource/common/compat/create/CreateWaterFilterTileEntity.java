package gloridifice.watersource.common.compat.create;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import com.simibubi.create.foundation.item.SmartInventory;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;
import gloridifice.watersource.common.item.StrainerBlockItem;
import gloridifice.watersource.data.ModItemTags;
import gloridifice.watersource.registry.FluidRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class CreateWaterFilterTileEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    public static final int TANK_SIZE = 2000;
    public SmartFluidTankBehaviour inputTank;
    public SmartFluidTankBehaviour outputTank;
    LazyOptional<ItemStackHandler> strainer = LazyOptional.of(this::createStrainerItemStackHandler);
    int tick = 0;
    public LazyOptional<ItemStackHandler> getStrainer() {
        return strainer;
    }

    private ItemStackHandler createStrainerItemStackHandler() {
        return new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
                clientSync();
            }
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return ForgeRegistries.ITEMS.tags().getTag(ModItemTags.STRAINERS).contains(stack.getItem());
            }
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (stack.isEmpty()) return ItemStack.EMPTY;
                if (!isItemValid(slot, stack)) return stack;
                validateSlotIndex(slot);
                ItemStack existing = this.stacks.get(slot);
                if (simulate) {
                    if (!existing.isEmpty()) {
                        Block.popResource(level, getBlockPos(), existing);
                        extractItem(slot, existing.getCount() + 1, false);
                    }
                    this.stacks.set(slot, stack);
                }
                onContentsChanged(slot);
                return ItemStack.EMPTY;
            }
            @Override
            public int getSlotLimit(int slot)
            {
                return 1;
            }
            @Override
            protected int getStackLimit(int slot, @Nonnull ItemStack stack) {
                return 1;
            }
        };
    }

    public void clientSync() {
        if (Objects.requireNonNull(this.getLevel()).isClientSide) {
            return;
        }
        ServerLevel world = (ServerLevel) this.getLevel();
        Stream<ServerPlayer> entities = world.getChunkSource().chunkMap.getPlayers(new ChunkPos(this.worldPosition), false).stream();
        ClientboundBlockEntityDataPacket updatePacket = this.getUpdatePacket();
        entities.forEach(e -> {
            if (updatePacket != null) {
                e.connection.send(updatePacket);
            }
        });
    }


    public CreateWaterFilterTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        inputTank = SmartFluidTankBehaviour.single(this, TANK_SIZE);
        behaviours.add(inputTank);
        outputTank = SmartFluidTankBehaviour.single(this, TANK_SIZE);
        behaviours.add(outputTank);

    }
    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().expandTowards(0, -2, 0);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && side != null && side.getAxis() == Direction.Axis.Y) {
            if (side == Direction.DOWN) return outputTank.getCapability().cast();
            else return inputTank.getCapability().cast();
        }
        if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY == cap) {
            return strainer.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void notifyUpdate() {
        super.notifyUpdate();
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputTank.getCapability().invalidate();
        outputTank.getCapability().invalidate();
        strainer.invalidate();
    }
    @Override
    public void destroy() {
        super.destroy();
        ItemHelper.dropContents(this.level, this.worldPosition, this.strainer.orElse(null));
    }
    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        tag.put("strainer", this.strainer.orElse(null).serializeNBT());
        super.write(tag, clientPacket);
    }
    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        this.strainer.orElse(null).deserializeNBT(tag.getCompound("strainer"));
        super.read(tag, clientPacket);
    }

    public void tick() {
        super.tick();
        if (
                !level.isClientSide
                && inputTank.getPrimaryHandler().getFluidAmount() >= 2
                && outputTank.getPrimaryHandler().getFluidAmount() < TANK_SIZE
        ) {
            ItemStack stack = strainer.orElse(null).getStackInSlot(0);
            FluidStack water = inputTank.getPrimaryHandler().drain(2, IFluidHandler.FluidAction.EXECUTE);

            if ((water.getFluid().isSame(Fluids.WATER) || inputTank.getPrimaryHandler().getFluid().getFluid().isSame(Fluids.WATER)) && !stack.isEmpty()) {
                if (stack.is(ModItemTags.PURIFICATION_STRAINERS)) {
                    FluidStack pure = new FluidStack(FluidRegistry.PURIFIED_WATER.get(), 2);
                    outputTank.getPrimaryHandler().fill(pure, IFluidHandler.FluidAction.EXECUTE);
                }
                else if (stack.is(ModItemTags.SOUL_STRAINERS)) {
                    FluidStack soul = new FluidStack(FluidRegistry.SOUL_WATER.get(), 2);
                    outputTank.getPrimaryHandler().fill(soul, IFluidHandler.FluidAction.EXECUTE);
                }
                tick++;
                if (tick == 500) {
                    tick = 0;
                    if (stack.isDamageableItem()) {
                        strainer.orElse(null).setStackInSlot(0, StrainerBlockItem.hurt(stack, 2));
                    }
                }
            }
            else {
                outputTank.getPrimaryHandler().fill(water, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        // MB
        LangBuilder mb = Lang.translate("generic.unit.millibuckets");

        // TILE
        Lang.translate("gui.goggles.fluid_container").forGoggles(tooltip);

        int inputAmount = inputTank.getPrimaryHandler().getFluidAmount();
        int outputAmount = outputTank.getPrimaryHandler().getFluidAmount();
        ItemStack stack = strainer.orElse(null).getStackInSlot(0);

        buildItemToolTip(tooltip, stack);
        buildFluidTooltip(tooltip, mb, inputAmount, inputTank);
        buildFluidTooltip(tooltip, mb, outputAmount, outputTank);

        if (inputTank.isEmpty() && outputTank.isEmpty()) {
            Lang.translate("gui.goggles.fluid_container.capacity")
                    .add(Lang.number(inputTank.getPrimaryHandler().getTankCapacity(0))
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);
        }

        return !outputTank.isEmpty() || !inputTank.isEmpty() || !strainer.orElse(null).getStackInSlot(0).isEmpty();
    }

    private void buildItemToolTip(List<Component> tooltip, ItemStack stack) {
        if (!stack.isEmpty()) {
            Lang.builder()
                    .add(Lang.text(""))
                    .add(Components.translatable(stack.getDescriptionId()).withStyle(ChatFormatting.GRAY))
                    .add(Lang.text(" x" + stack.getCount()).style(ChatFormatting.GREEN))
                    .forGoggles(tooltip, 1);
        }
    }

    private void buildFluidTooltip(List<Component> tooltip, LangBuilder mb, int amount, SmartFluidTankBehaviour tank) {
        if(!tank.isEmpty())
        {
            Lang.builder()
                    .add(Lang.text(" "))
                    .add(Lang.fluidName(tank.getPrimaryHandler().getFluid()))
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip);

            Lang.builder()
                    .add(Lang.number(amount)
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(Lang.number(tank.getPrimaryHandler().getCapacity())
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
        }
    }
}
