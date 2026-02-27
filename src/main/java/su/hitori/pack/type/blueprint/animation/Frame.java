package su.hitori.pack.type.blueprint.animation;

import com.mojang.math.Transformation;

import java.util.Map;
import java.util.UUID;

public record Frame(Map<UUID, Transformation> nodeTransformations) {
}
