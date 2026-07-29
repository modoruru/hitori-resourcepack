package su.hitori.pack.pose.crawl;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.entity.Player;
import su.hitori.pack.pose.PoseService;

public final class CrawlCommand {

    private CrawlCommand() {}

    public static LiteralCommandNode<CommandSourceStack> bootstrap(PoseService poseService) {
        return Commands.literal("crawl")
                .requires(source -> source.getSender() instanceof Player)
                .executes(context -> {
                    Player sender = (Player) context.getSource().getSender();

                    CrawlPose crawlPose = poseService.getCrawlPoseByCrawling(sender);
                    if(crawlPose != null) {
                        poseService.removeCrawlPose(crawlPose);
                        return 0;
                    }

                    if(!sender.isValid() || !sender.isOnGround() || sender.getVehicle() != null || sender.isSleeping())
                        return 0;

                    poseService.createCrawlPose(sender);

                    return 1;
                })
                .build();
    }
}
