package gloridifice.watersource.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Function;

import static gloridifice.watersource.WaterSource.NETWORK_VERSION;

public final class SimpleNetworkHandler {
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(new ResourceLocation("watersource", "main")).networkProtocolVersion(() -> "1.0").serverAcceptedVersions("1.0"::equals).clientAcceptedVersions("1.0"::equals).simpleChannel();

    public SimpleNetworkHandler() {

    }
    public static void init() {
        int id = 0;
        registerMessage(id++, PlayerWaterLevelMessage.class, PlayerWaterLevelMessage::new);
        registerMessage(id++, DrinkWaterMessage.class, DrinkWaterMessage::new);
    }

    private static <T extends INormalMessage> void registerMessage(int index, Class<T> messageType, Function<FriendlyByteBuf, T> decoder) {
        CHANNEL.registerMessage(index, messageType, INormalMessage::toBytes, decoder, (message, context) -> {
            message.process(context);
            context.get().setPacketHandled(true);
        });
    }
}
