package su.hitori.pack.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Messages;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.blueprint.Blueprints;
import su.hitori.pack.type.blueprint.Blueprint;
import su.hitori.pack.type.blueprint.animation.Animation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class BlueprintCommand extends CommandAPICommand {

    private final Blueprints blueprints;
    private final Registry<@NotNull Blueprint> blueprintRegistry;
    private final Map<UUID, BlueprintInstance> blueprintInstances;
    private final Component blueprintInstanceDoesNotExistsMessage;

    public BlueprintCommand(Blueprints blueprints, Registry<@NotNull Blueprint> blueprintRegistry) {
        super("test-blueprint");
        this.blueprints = blueprints;
        this.blueprintRegistry = blueprintRegistry;
        this.blueprintInstances = new HashMap<>();
        this.blueprintInstanceDoesNotExistsMessage = Messages.ERROR.create("Such blueprint instance does not exists");

        Argument<UUID> blueprintInstanceArgument = new UUIDArgument("blueprint_instance").replaceSafeSuggestions(SafeSuggestions.suggestCollection(
                _ -> blueprintInstances.keySet()
        ));

        withSubcommands(
                new CommandAPICommand("summon")
                        .withArguments(
                                new NamespacedKeyArgument("blueprint").replaceSuggestions(ArgumentSuggestions.stringCollection(
                                        _ -> blueprintRegistry.keys().stream().map(Key::asString).toList()
                                ))
                        )
                        .withOptionalArguments(new LocationArgument("location"))
                        .executes(this::summon),

                new CommandAPICommand("destroy")
                        .withArguments(blueprintInstanceArgument)
                        .executes(this::destroy),

                new CommandAPICommand("add_observer")
                        .withArguments(blueprintInstanceArgument, new EntitySelectorArgument.OnePlayer("observer"))
                        .executes(this::addObserver),

                new CommandAPICommand("remove_observer")
                        .withArguments(blueprintInstanceArgument, new EntitySelectorArgument.OnePlayer("observer"))
                        .executes(this::removeObserver),

                new CommandAPICommand("play")
                        .withArguments(blueprintInstanceArgument, new TextArgument("animation_name").replaceSuggestions(ArgumentSuggestions.stringCollection(
                                info -> {
                                    BlueprintInstance blueprintInstance = extractBlueprintInstance(info.previousArgs());
                                    if(blueprintInstance == null) return List.of();

                                    return blueprintInstance.blueprint()
                                            .animations()
                                            .values()
                                            .stream()
                                            .map(animation -> animation.name)
                                            .toList();
                                }
                        )))
                        .executes(this::play)
        );
    }

    private void summon(CommandSender sender, CommandArguments args) {
        Location location = (Location) args.get("location");

        if(location == null) {
            if(!(sender instanceof Player player)) {
                sender.sendMessage(Messages.ERROR.create("Specify a location to summon blueprint."));
                return;
            }

            location = player.getLocation();
        }

        NamespacedKey blueprintKey = (NamespacedKey) args.get("blueprint");
        assert blueprintKey != null;

        Blueprint blueprint = blueprintRegistry.get(blueprintKey);
        if(blueprint == null) {
            sender.sendMessage(Messages.ERROR.create("Blueprint \"" + blueprintKey.asString() + "\" does not exists."));
            return;
        }

        UUID uuid = UUID.randomUUID();
        BlueprintInstance instance = blueprints.instantiate(blueprint)
                .move(location);

        if(sender instanceof Player player)
            instance.addObserver(player).assignSkin(0, player.getPlayerProfile());

        instance.show();

        blueprintInstances.put(uuid, instance);
        sender.sendMessage(Messages.INFO.create("Summoned \"" + blueprintKey.asString() + "\" blueprint instance."));
    }

    private void destroy(CommandSender sender, CommandArguments args) {
        BlueprintInstance blueprintInstance = blueprintInstances.remove((UUID) args.get("blueprint_instance"));
        if(blueprintInstance == null) {
            sender.sendMessage(blueprintInstanceDoesNotExistsMessage);
            return;
        }

        blueprintInstance.destroy();
        sender.sendMessage(Messages.INFO.create("Blueprint instance destroyed"));
    }

    private BlueprintInstance extractBlueprintInstance(CommandArguments args) {
        return blueprintInstances.get((UUID) args.get("blueprint_instance"));
    }

    private void addObserver(CommandSender sender, CommandArguments args) {
        BlueprintInstance blueprintInstance = extractBlueprintInstance(args);
        if(blueprintInstance == null) {
            sender.sendMessage(blueprintInstanceDoesNotExistsMessage);
            return;
        }

        Player observer = (Player) args.get("observer");
        assert observer != null;
        if(blueprintInstance.observing(observer)) {
            sender.sendMessage(Messages.ERROR.create("<yellow>" + observer.getName() + "</yellow> is already observing."));
            return;
        }

        blueprintInstance.addObserver(observer);
        sender.sendMessage(Messages.INFO.create("<yellow>" + observer.getName() + "</yellow> is now observing blueprint instance."));
    }

    private void removeObserver(CommandSender sender, CommandArguments args) {
        BlueprintInstance blueprintInstance = extractBlueprintInstance(args);
        if(blueprintInstance == null) {
            sender.sendMessage(blueprintInstanceDoesNotExistsMessage);
            return;
        }

        Player observer = (Player) args.get("observer");
        assert observer != null;
        if(!blueprintInstance.observing(observer)) {
            sender.sendMessage(Messages.ERROR.create("<yellow>" + observer.getName() + "</yellow> is not observing this blueprint instance."));
            return;
        }

        blueprintInstance.removeObserver(observer);
        sender.sendMessage(Messages.INFO.create("<yellow>" + observer.getName() + "</yellow> is no longer observing blueprint instance."));
    }

    private void play(CommandSender sender, CommandArguments args) {
        BlueprintInstance blueprintInstance = extractBlueprintInstance(args);
        if(blueprintInstance == null) {
            sender.sendMessage(blueprintInstanceDoesNotExistsMessage);
            return;
        }

        Animation animation = blueprintInstance.blueprint().animation((String) args.get("animation_name")).orElse(null);
        if(animation == null) {
            sender.sendMessage(Messages.ERROR.create("Animation does not exists."));
            return;
        }

        blueprintInstance.play(animation);
    }

}
