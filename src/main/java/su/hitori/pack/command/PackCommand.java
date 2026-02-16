package su.hitori.pack.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.GreedyStringArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import su.hitori.api.util.Messages;
import su.hitori.api.util.Text;
import su.hitori.pack.BuiltInConveyors;
import su.hitori.pack.PackModule;

public final class PackCommand extends CommandAPICommand {

    public PackCommand(PackModule packModule) {
        super("pack");
        withPermission("*");
        withSubcommands(
                new CommandAPICommand("test-send")
                        .withArguments(new GreedyStringArgument("text"))
                        .executes(this::testSend),

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
                        }),

                new CommandAPICommand("test-spawn")
                        .withArguments(new IntegerArgument("amount", 1))
                        .executesPlayer((sender, args) -> {
                            int amount = args.getOrDefaultUnchecked("amount", 1);
                            sender.sendMessage(Messages.INFO.create("Spawned <aqua>" + amount + "</aqua> test block-entities."));

                            Location loc = sender.getLocation();
                            for (int i = 0; i < amount; i++) {
                                loc.getWorld().spawn(loc, Interaction.class);
                                loc.getWorld().spawn(loc, ItemDisplay.class);
                            }
                        })
        );
    }

    private void testSend(CommandSender sender, CommandArguments args) {
        String text = args.getUnchecked("text");
        sender.sendMessage(Text.create(text));
    }

}
