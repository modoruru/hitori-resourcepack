package su.hitori.pack.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.*;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.key.Key;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Messages;
import su.hitori.api.util.Text;
import su.hitori.pack.BuiltInConveyors;
import su.hitori.pack.PackModule;
import su.hitori.pack.blueprint.BlueprintInstance;
import su.hitori.pack.blueprint.Blueprints;
import su.hitori.pack.type.blueprint.Blueprint;

import java.util.HashMap;
import java.util.Map;

public final class PackCommand extends CommandAPICommand {

    public PackCommand(PackModule packModule, Blueprints blueprints, Registry<@NotNull Blueprint> blueprintRegistry) {
        super("pack");

        withPermission("*");
        withSubcommands(
                new CommandAPICommand("test-send")
                        .withArguments(new GreedyStringArgument("text"))
                        .executes(this::testSend),

                new BlueprintCommand(blueprints, blueprintRegistry),

                new GiveCommand(
                        packModule.registryAccess()
                                .access(BuiltInConveyors.CUSTOM_ITEM)
                                .orElseThrow()
                ),

                new CommandAPICommand("generate")
                        .executes((sender, _) -> {
                            sender.sendMessage(Text.create(
                                    packModule.generator().generate()
                                            ? "Starting generation of resourcepack"
                                            : "Already generating"
                            ));
                        })
        );
    }



    private void testSend(CommandSender sender, CommandArguments args) {
        String text = args.getUnchecked("text");
        sender.sendMessage(Text.create(text));
    }

}
