package su.hitori.pack.block.level;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.*;

public final class LevelServiceListener implements Listener {

    private final LevelService levelService;

    public LevelServiceListener(LevelService levelService) {
        this.levelService = levelService;
    }

    @EventHandler
    private void onChunkLoad(ChunkLoadEvent event) {
        levelService.loadChunk(event.getChunk());
    }

    @EventHandler
    private void onChunkUnload(ChunkUnloadEvent event) {
        levelService.unloadChunk(event.getChunk(), event.isSaveChunk());
    }

    @EventHandler
    private void onWorldSave(WorldLoadEvent event) {
        levelService.loadLevel(event.getWorld());
    }

    @EventHandler
    private void onWorldSave(WorldSaveEvent event) {
        levelService.saveLevel(event.getWorld());
    }

    @EventHandler
    private void onWorldUnload(WorldUnloadEvent event) {
        levelService.unloadLevel(event.getWorld());
    }

}
