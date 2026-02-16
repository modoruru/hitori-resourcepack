package su.hitori.pack.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.NamespacedKeyArgument;
import dev.jorel.commandapi.executors.CommandArguments;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Messages;
import su.hitori.api.util.Text;
import su.hitori.pack.type.item.CustomItem;

public final class GiveCommand extends CommandAPICommand {

    private final Registry<@NotNull CustomItem> registry;

    public GiveCommand(Registry<@NotNull CustomItem> registry) {
        super("give");
        this.registry = registry;

        withArguments(new EntitySelectorArgument.OnePlayer("target"), new NamespacedKeyArgument("item").replaceSuggestions(ArgumentSuggestions.strings(
                (_) -> registry.keys().stream().map(Key::asString).filter(string -> !string.startsWith("_")).toList().toArray(new String[0])
        ))).withOptionalArguments(new IntegerArgument("amount", 0, 99));

        executes(this::execute);
    }

    private void execute(CommandSender sender, CommandArguments args) {
        Player target = (Player) args.get("target");
        String item = args.getRaw("item");
        assert target != null && item != null;

        CustomItem customItem = registry.get(Key.key(item));
        if(customItem == null) {
            sender.sendMessage(Text.create(String.format(
                    "<color:red><lang:argument.item.id.invalid:%s><br><gray>...%s</gray> <u>%s</u><italic><lang:command.context.here></color>",
                    item,
                    target.getName(),
                    item
            )));
            return;
        }

        ItemStack instance = customItem.create();

        if(args.get("amount") instanceof Integer integer) instance.setAmount(integer);
        target.getInventory().addItem(instance);
        sender.sendMessage(Messages.INFO.translatable(
                "commands.give.success.single",
                Component.text(instance.getAmount()),
                instance.displayName(),
                Component.text(target.getName())
        ));
    }

}
