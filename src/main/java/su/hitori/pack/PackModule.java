package su.hitori.pack;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import su.hitori.api.module.Module;
import su.hitori.api.module.enable.EnableContext;
import su.hitori.api.registry.Registry;
import su.hitori.api.registry.RegistryAccess;
import su.hitori.pack.block.level.LevelService;
import su.hitori.pack.block.level.LevelServiceListener;
import su.hitori.pack.block.player.CustomBlockListener;
import su.hitori.pack.block.protection.CombinedProtectionService;
import su.hitori.pack.block.protection.CoreProtectSupport;
import su.hitori.pack.command.PackCommand;
import su.hitori.pack.generation.Generator;
import su.hitori.pack.host.PackListener;
import su.hitori.pack.host.PackServer;
import su.hitori.pack.impl.GeneratorImpl;
import su.hitori.pack.pose.PoseService;
import su.hitori.pack.pose.crawl.CrawlCommand;
import su.hitori.pack.pose.crawl.CrawlListener;
import su.hitori.pack.pose.lie.LayCommand;
import su.hitori.pack.pose.lie.LyingPoseListener;
import su.hitori.pack.pose.listener.BlockListener;
import su.hitori.pack.pose.listener.EntityListener;
import su.hitori.pack.pose.listener.PlayerListener;
import su.hitori.pack.pose.listener.SeatListener;
import su.hitori.pack.pose.seat.SitCommand;
import su.hitori.pack.type.block.CustomBlock;
import su.hitori.pack.type.item.CustomItem;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class PackModule extends Module {

    private ExecutorService executorService;
    private GeneratorImpl generator;
    private PackServer packServer;
    private BuiltInConveyors builtInConveyors;
    private PoseService poseService;

    private LevelService levelService;
    private TextSupport textSupport;

    private CoreProtectSupport coreProtectSupport;

    @Override
    public void enable(EnableContext context) {
        new PackConfiguration(defaultConfig()).reload();

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        generator = new GeneratorImpl(this, folder().toFile());
        packServer = new PackServer(generator);
        builtInConveyors = new BuiltInConveyors(this);
        poseService = new PoseService();

        Registry<@NotNull CustomBlock> customBlockRegistry = builtInConveyors.access(BuiltInConveyors.CUSTOM_BLOCK).get();
        Registry<@NotNull CustomItem> customItemRegistry = builtInConveyors.access(BuiltInConveyors.CUSTOM_ITEM).get();

        CombinedProtectionService combinedProtectionService = new CombinedProtectionService();
        levelService = new LevelService(this, combinedProtectionService, customBlockRegistry, customItemRegistry);
        textSupport = new TextSupport(builtInConveyors.access(BuiltInConveyors.GLYPH).get());

        if(PackConfiguration.I.coreProtectSupport)
            coreProtectSupport = CoreProtectSupport.create(Bukkit.getPluginManager()).orElse(null);

        createSkinsRestorerSupport();

        context.listeners().register(
                new PackListener(packServer),
                new LevelServiceListener(levelService),
                new CustomBlockListener(
                        customBlockRegistry,
                        customItemRegistry,
                        levelService
                ),

                new BlockListener(this),
                new EntityListener(this),
                new SeatListener(poseService),
                new PlayerListener(poseService),
                new LyingPoseListener(poseService),
                new CrawlListener(poseService)
        );
        context.commands().register(
                new PackCommand(this),
                new SitCommand(this),
                new LayCommand(poseService),
                new CrawlCommand(poseService)
        );
        generator.generate();
        packServer.start();
        combinedProtectionService.load();
        levelService.load();
        textSupport.load();
    }

    private void createSkinsRestorerSupport() {
        if(Bukkit.getPluginManager().getPlugin("SkinsRestorer") == null) return;
        new SkinsRestorerSupport(poseService).initialize();
    }

    @Override
    public void disable() {
        packServer.stop();
        levelService.unload();
        textSupport.unload();
        poseService.removeAllPoses();
    }

    public ExecutorService executorService() {
        return executorService;
    }

    public Generator generator() {
        return generator;
    }

    public PackServer packServer() {
        return packServer;
    }

    public RegistryAccess registryAccess() {
        return builtInConveyors;
    }

    public PoseService poseService() {
        return poseService;
    }

    public Optional<CoreProtectSupport> coreProtectSupport() {
        return Optional.ofNullable(coreProtectSupport);
    }

}
