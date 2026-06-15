package su.hitori.pack.type.block;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;

public record CustomBlock(BlockProperties blockProperties) implements Keyed {

    public CustomBlock {
        if(blockProperties.placementProperties() == null) throw new IllegalArgumentException("placement properties should not be null");
        if(blockProperties.behaviourProperties() == null) throw new IllegalArgumentException("behaviour properties should not be null");
    }

    @Override
    public Key key() {
        return blockProperties.key();
    }

}
