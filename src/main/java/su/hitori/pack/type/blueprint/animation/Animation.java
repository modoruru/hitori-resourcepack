package su.hitori.pack.type.blueprint.animation;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class Animation {

    public final UUID uuid;
    public final String name;
    public final int duration;
    public final int loopDelay;
    public final LoopMode loopMode;
    private final Map<Integer, Frame> frames;

    public Animation(UUID uuid, String name, int duration, int loopDelay, LoopMode loopMode, Map<Integer, Frame> frames) {
        this.uuid = uuid;
        this.name = name;
        this.duration = duration;
        this.loopDelay = loopDelay;
        this.loopMode = loopMode;
        this.frames = Map.copyOf(frames);
    }

    public Optional<Frame> getFrame(int frame) {
        if(frame < 0 || frame > duration) return Optional.empty();
        return Optional.ofNullable(frames.get(frame));
    }

}
