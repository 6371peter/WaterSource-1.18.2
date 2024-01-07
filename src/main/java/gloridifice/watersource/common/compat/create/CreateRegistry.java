package gloridifice.watersource.common.compat.create;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import gloridifice.watersource.WaterSource;
import gloridifice.watersource.common.compat.create.client.CreateWaterFilterRenderer;
import gloridifice.watersource.registry.CreativeModeTabRegistry;
import net.minecraft.client.renderer.RenderType;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

public class CreateRegistry {
    public static final NonNullSupplier<Registrate> REGISTRATE=NonNullSupplier.lazy(() ->Registrate.create(WaterSource.MODID));

    public static void register(){}

    static {
        REGISTRATE.get().creativeModeTab(() -> CreativeModeTabRegistry.WATER_SOURCE_TAB);
    }

    public static final BlockEntry<CreateWaterFilterBlock> CREATE_WATER_FILTER_BLOCK = REGISTRATE.get()
            .block("create_water_filter", CreateWaterFilterBlock::new)
            .initialProperties(SharedProperties::copperMetal)
            .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), AssetLookup.partialBaseModel(ctx, prov)))
            .addLayer(() -> {return RenderType::cutoutMipped;})
            .item(AssemblyOperatorBlockItem::new)
            .transform(customItemModel())
            .register();

    public static final BlockEntityEntry<CreateWaterFilterTileEntity> CREATE_WATER_FILTER_TE = REGISTRATE.get()
            .blockEntity("create_water_filter", CreateWaterFilterTileEntity::new)
            .renderer(() -> {return CreateWaterFilterRenderer::new;})
            .validBlocks(CREATE_WATER_FILTER_BLOCK)
            .register();
}
