package su.hitori.pack.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import su.hitori.api.registry.Registry;
import su.hitori.api.util.Messages;
import su.hitori.api.util.Text;
import su.hitori.pack.type.item.CustomItem;

public final class GiveCommand {

    private GiveCommand() {}

    public static LiteralCommandNode<CommandSourceStack> bootstrap(Registry<CustomItem> registry) {
        return Commands.literal("give")
                .then(Commands.argument("target", ArgumentTypes.player())
                        .then(Commands.argument("item", ArgumentTypes.namespacedKey())
                                .suggests((_, builder) -> {
                                    registry.keys().stream().map(Key::asString).filter(string -> !string.startsWith("_")).forEach(builder::suggest);
                                    return builder.buildFuture();
                                })
                                .executes(context -> execute(registry, context, false))
                                .then(Commands.argument("amount", IntegerArgumentType.integer(0, 99))
                                        .executes(context -> execute(registry, context, true)))))
                .build();
    }

    private static int execute(Registry<CustomItem> registry, CommandContext<CommandSourceStack> context, boolean amount) throws CommandSyntaxException {
        PlayerSelectorArgumentResolver targetResolver = context.getArgument("target", PlayerSelectorArgumentResolver.class);
        Player target = targetResolver.resolve(context.getSource()).getFirst();

        NamespacedKey item = context.getArgument("item", NamespacedKey.class);

        CustomItem customItem = registry.get(item);
        if(customItem == null) {
            context.getSource().getSender().sendMessage(Text.create(String.format(
                    "<color:red><lang:argument.item.id.invalid:%s><br><gray>...%s</gray> <u>%s</u><italic><lang:command.context.here></color>",
                    item.asString(),
                    target.getName(),
                    item
            )));
            return 0;
        }

        ItemStack instance = customItem.create();

        if(amount) instance.setAmount(context.getArgument("amount", Integer.class));

        target.getInventory().addItem(instance);

        context.getSource().getSender().sendMessage(Messages.INFO.translatable(
                "commands.give.success.single",
                Component.text(instance.getAmount()),
                instance.displayName(),
                Component.text(target.getName())
        ));
        return 1;
    }

}
