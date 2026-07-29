package su.hitori.pack;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.jspecify.annotations.Nullable;
import su.hitori.api.configuration.ConfigurationSource;
import su.hitori.api.configuration.HitoriConfiguration;
import su.hitori.api.configuration.serializer.YAMLSerializer;
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

    private final PackConfiguration configuration;

    private @Nullable ExecutorService executorService;
    private @Nullable GeneratorImpl generator;
    private @Nullable PackServer packServer;
    private @Nullable BuiltInConveyors builtInConveyors;
    private @Nullable PoseService poseService;

    private @Nullable LevelService levelService;
    private @Nullable TextSupport textSupport;

    private @Nullable CoreProtectSupport coreProtectSupport;

    public PackModule() {
        configuration = new PackConfiguration();
    }

    @Override
    public void enable(EnableContext context) {
        HitoriConfiguration<PackConfiguration> configuration = context.configurations().register(
                Key.key("hitori:resourcepack"),
                this.configuration,
                ConfigurationSource.file(YAMLSerializer.INSTANCE, defaultConfig())
        );

        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        generator = new GeneratorImpl(this, folder().toFile());
        packServer = new PackServer(configuration.access(), generator);
        builtInConveyors = new BuiltInConveyors(this);
        poseService = new PoseService();

        Registry<CustomBlock> customBlockRegistry = builtInConveyors.access(BuiltInConveyors.CUSTOM_BLOCK).get();
        Registry<CustomItem> customItemRegistry = builtInConveyors.access(BuiltInConveyors.CUSTOM_ITEM).get();

        CombinedProtectionService combinedProtectionService = new CombinedProtectionService();
        levelService = new LevelService(this, combinedProtectionService, customBlockRegistry, customItemRegistry);
        textSupport = new TextSupport(builtInConveyors.access(BuiltInConveyors.GLYPH).get());

        if(configuration.access().coreProtectSupport.get())
            coreProtectSupport = CoreProtectSupport.create(Bukkit.getPluginManager()).orElse(null);

        createSkinsRestorerSupport();

        context.listeners().register(
                new PackListener(configuration.access(), packServer),
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
                PackCommand.bootstrap(this),
                SitCommand.bootstrap(this),
                LayCommand.bootstrap(poseService),
                CrawlCommand.bootstrap(poseService)
        );

        packServer.start();
        combinedProtectionService.load();
        levelService.load();
        textSupport.load();

        context.enableHooksFuture().thenAccept(_ -> generator.generate());
    }

    private void createSkinsRestorerSupport() {
        if(Bukkit.getPluginManager().getPlugin("SkinsRestorer") == null) return;
        assert poseService != null;
        new SkinsRestorerSupport(configuration, poseService).initialize();
    }

    @Override
    public void disable() {
        assert packServer != null && levelService != null && textSupport != null && poseService != null;
        packServer.stop();
        levelService.unload();
        textSupport.unload();
        poseService.removeAllPoses();
    }

    public ExecutorService executorService() {
        assert executorService != null;
        return executorService;
    }

    public Generator generator() {
        assert generator != null;
        return generator;
    }

    public PackServer packServer() {
        assert packServer != null;
        return packServer;
    }

    public RegistryAccess registryAccess() {
        assert builtInConveyors != null;
        return builtInConveyors;
    }

    public PoseService poseService() {
        assert poseService != null;
        return poseService;
    }

    public Optional<CoreProtectSupport> coreProtectSupport() {
        return Optional.ofNullable(coreProtectSupport);
    }

}
