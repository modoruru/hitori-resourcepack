package su.hitori.pack.pose.crawl;

import dev.jorel.commandapi.CommandAPICommand;
import su.hitori.pack.pose.PoseService;

public final class CrawlCommand extends CommandAPICommand {

    public CrawlCommand(PoseService poseService) {
        super("crawl");
        executesPlayer((sender, args) -> {
            CrawlPose crawlPose = poseService.getCrawlPoseByCrawling(sender);
            if(crawlPose != null) {
                poseService.removeCrawlPose(crawlPose);
                return;
            }

            if(!sender.isValid() || !sender.isOnGround() || sender.getVehicle() != null || sender.isSleeping())
                return;

            poseService.createCrawlPose(sender);
        });
    }

}
