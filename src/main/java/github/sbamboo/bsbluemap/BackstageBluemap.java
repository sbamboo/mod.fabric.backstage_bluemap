package github.sbamboo.bsbluemap;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackstageBluemap implements ModInitializer {
	public static final String MOD_ID = "backstage_bluemap";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// Load configuration
		ConfigManager.loadConfig();

		// Get the mod container for this mod ID
		ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).orElse(null);

		// Retrieve the version from the mod metadata or set to "unknown"
		String version = (modContainer != null) ? modContainer.getMetadata().getVersion().getFriendlyString() : "unknown";

		// Retrieve authors and join them with "&"
		String authors = (modContainer != null) ? modContainer.getMetadata().getAuthors().stream()
				.map(Person::getName)
                .collect(Collectors.joining("&")) : "unknown";
		authors = capitalizeFirstLetter(authors);

		LOGGER.info(authors + "'s Backstage Bluemap " + version + " loaded!");

		// Register player join and leave events
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> onPlayerJoin(server));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> onPlayerLeave(server));

		// Register the commands
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("bsbluemap")
					.then(CommandManager.literal("reload")
							.executes(context -> reloadConfig(context.getSource()))
					)
					.then(CommandManager.literal("enable")
							.executes(context -> setConfigEnabled(context.getSource(), true))
					)
					.then(CommandManager.literal("disable")
							.executes(context -> setConfigEnabled(context.getSource(), false))
					)
					.then(CommandManager.literal("query")
							.executes(context -> queryConfig(context.getSource()))
					)
			);
		});
	}

	private int reloadConfig(ServerCommandSource source) {
		// Call your config reload logic here
		ConfigManager.loadConfig();

		// Create a Text object for the feedback message
		Text feedbackMessage = Text.of("Configuration reloaded successfully!");

		// Send the feedback message back to the player
		source.sendFeedback(() -> feedbackMessage, false);
		return 1; // Return success
	}

	private int setConfigEnabled(ServerCommandSource source, boolean enabled) {
		ConfigManager.enabled = enabled;
		Properties properties = new Properties();
		properties.setProperty("enabled", String.valueOf(enabled));
		ConfigManager.saveConfig(properties); // Save updated config
		if (enabled) {
			Text feedbackMessage = Text.of("Enabled, bluemap will now be stopped when players are online!");
			source.sendFeedback(() -> feedbackMessage, false);
		} else {
			Text feedbackMessage = Text.of("Disabled, bluemap is now manually controlled!");
			source.sendFeedback(() -> feedbackMessage, false);
		}
		return 1; // Return success
	}

	private int queryConfig(ServerCommandSource source) {
		if (ConfigManager.enabled) {
			Text feedbackMessage = Text.of("Enabled, bluemap is active when no players are online.");
			source.sendFeedback(() -> feedbackMessage, false);
		} else {
			Text feedbackMessage = Text.of("Disabled, bluemap is manually controlled.");
			source.sendFeedback(() -> feedbackMessage, false);
		}
		return 1; // Return success
	}

	private void onPlayerJoin(MinecraftServer server) {
		// Check if the mod actions are enabled
		if (ConfigManager.enabled) {
			int playerCount = server.getCurrentPlayerCount() + 1; // Adjust for joining player
			LOGGER.info("Player count: " + playerCount);

			if (playerCount > 0) {
				server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/bluemap stop");
			}
		} else {
			LOGGER.info("Automatic managment of bluemap is disabled in config.");
		}
	}

	private void onPlayerLeave(MinecraftServer server) {
		// Check if the mod actions are enabled
		if (ConfigManager.enabled) {
			int playerCount = server.getCurrentPlayerCount() - 1; // Adjust for leaving player
			LOGGER.info("Player count: " + playerCount);

			if (playerCount < 1) {
				server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/bluemap start");
			}
		} else {
			LOGGER.info("Automatic managment of bluemap is disabled in config.");
		}
	}

	// Method to capitalize the first letter of a string
	private String capitalizeFirstLetter(String str) {
		if (str == null || str.isEmpty()) {
			return str;
		}
		return Character.toUpperCase(str.charAt(0)) + str.substring(1);
	}
}