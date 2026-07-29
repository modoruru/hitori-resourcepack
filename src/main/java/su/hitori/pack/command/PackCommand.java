package su.hitori.pack.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import su.hitori.api.util.Text;
import su.hitori.pack.BuiltInConveyors;
import su.hitori.pack.PackModule;

public final class PackCommand {

    private PackCommand() {}

    public static LiteralCommandNode<CommandSourceStack> bootstrap(PackModule packModule) {
        return Commands.literal("pack")
                .requires(source -> source.getSender().hasPermission("*"))
                .then(Commands.literal("test-send")
                        .then(Commands.argument("text", StringArgumentType.greedyString())
                                .executes(context -> {
                                    context.getSource().getSender().sendMessage(Text.create(context.getArgument("text", String.class)));
                                    return 1;
                                })))
                .then(GiveCommand.bootstrap(
                        packModule.registryAccess()
                                .access(BuiltInConveyors.CUSTOM_ITEM)
                                .orElseThrow()
                ))
                .then(Commands.literal("generate")
                        .executes(context -> {
                            context.getSource().getSender().sendMessage(Text.create(
                                    packModule.generator().generate()
                                            ? "Starting generation of resourcepack"
                                            : "Already generating"
                            ));
                            return 1;
                        }))
                .build();
    }

}
