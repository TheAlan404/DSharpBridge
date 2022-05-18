package me.thegiggitybyte.wumpuslink;

import me.lucko.fabric.api.permissions.v0.Permissions;
import me.thegiggitybyte.wumpuslink.error.ConfigurationFieldMissingError;
import me.thegiggitybyte.wumpuslink.error.ConfigurationValueEmptyError;
import net.darktree.simpleconfig.SimpleConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.LiteralText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.server.command.CommandManager.literal;

public class WumpusLink implements DedicatedServerModInitializer {
    private static final String[] VALID_CONFIG_KEYS;
    private static Logger logger;
    private static SimpleConfig config;
    
    static {
        VALID_CONFIG_KEYS = new String[]{
                "discord-bot-token",
                "discord-channel-id",
                "discord-webhook-url"
        };
        
        logger = LoggerFactory.getLogger("wumpuslink");
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> initializeConfig());
    }
    
    @Override
    public void onInitializeServer() {
        WumpusLink.initialize();
    
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            var reloadCommand = literal("reload")
                    .requires(Permissions.require("wumpuslink.reload", 4)) // otherwise OP
                    .executes(ctx -> {
                        WumpusLink.initialize();
                        ctx.getSource().sendFeedback(new LiteralText("WumpusLink reload complete"), false);
                        return 1;
                    })
                    .build();
        
            var wumpusLinkCommand = literal("wumpuslink")
                    .then(reloadCommand)
                    .build();
        
            dispatcher.getRoot().addChild(wumpusLinkCommand);
        });
        
        logger.info("WumpusLink loaded :D");
    }
    
    static void initialize() {
        WumpusLink.initializeConfig();
        MessageProxy.initializeDiscord();
    }
    
    static SimpleConfig getConfig() {
        return config;
    }

    private static void initializeConfig() throws RuntimeException {
        config = SimpleConfig.of("wumpuslink")
                .provider(fileName -> getDefaultConfig())
                .request();
        
        for (var key : VALID_CONFIG_KEYS) {
            if (config.get(key) == null)
                throw new ConfigurationFieldMissingError(key);
            else if (config.get(key).trim().length() == 0)
                throw new ConfigurationValueEmptyError(key);
        }
    }
    
    private static String getDefaultConfig() {
        return """
                # Create application and with bot account at https://discord.com/developers/applications/
                discord-bot-token=
                
                # Discord chat channel
                discord-channel-id=
                
                # Used to send Minecraft chat messages to the above channel.
                discord-webhook-url=
                """;
    }
}