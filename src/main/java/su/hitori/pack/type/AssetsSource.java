package su.hitori.pack.type;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import su.hitori.api.util.Either;

import java.io.File;

/**
 * Source of assets to copy in resource pack
 * @param zipOrFolder zip archive or a folder containing all pack files
 */
public record AssetsSource(Key key, Either<File, File> zipOrFolder, CopyBehaviour copyBehaviour) implements Keyed {

    /**
     * Source of assets to copy in resource pack
     * @param zipOrFolder zip archive or a folder containing all pack files
     */
    public AssetsSource(Key key, Either<File, File> zipOrFolder) {
        this(key, zipOrFolder, CopyBehaviour.MERGE_OR_SKIP);
    }

    /**
     * Determines how this source of assets will act on file conflict
     */
    public enum CopyBehaviour {
        REPLACE,

        SKIP,

        /**
         * Will try to merge file if possible, replace if not.
         */
        MERGE_OR_REPLACE,

        /**
         * Will try to merge file if possible, skip if not.
         */
        MERGE_OR_SKIP
    }

}
