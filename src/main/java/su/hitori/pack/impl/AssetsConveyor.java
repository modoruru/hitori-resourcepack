package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import su.hitori.api.util.Either;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.type.AssetsSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class AssetsConveyor extends AbstractConveyor<AssetsSource> {

    public AssetsConveyor(Key key) {
        super(key);
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        for (Map.Entry<Key, AssetsSource> entry : snapshots.entrySet()) {
            AssetsSource source = entry.getValue();
            Either<File, File> zipOrFolder = source.zipOrFolder();

            if(zipOrFolder.firstPresent()) unzip(context.folder(), zipOrFolder.first(), source.copyBehaviour());
            else copyFolderContents(context.folder(), zipOrFolder.second(), source.copyBehaviour());
        }

        collectingOrGenerating = false;
    }

    private static void copyFolderContents(File destination, File folder, AssetsSource.CopyBehaviour behaviour) {
        if(!folder.exists()) return;

        Path folderPath = folder.toPath();

        List<File> files = getFilesRecursively(folder);
        for (File file : files) {
            File output = new File(
                    destination,
                    folderPath.relativize(file.toPath()).toString()
            );

            if(output.exists()) {
                switch (behaviour) {
                    case SKIP, MERGE_OR_SKIP -> {
                        continue;
                    }
                    default -> {}
                }
            }

            File parent = output.getParentFile();
            if(!parent.exists()) parent.mkdirs();

            try (FileOutputStream fos = new FileOutputStream(output); FileInputStream fis = new FileInputStream(file)) {
                fos.write(fis.readAllBytes());
                fos.flush();
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static List<File> getFilesRecursively(File folder) {
        if(!folder.exists()) return List.of();

        List<File> result = new ArrayList<>();
        File[] files = folder.listFiles();
        assert files != null;

        for (File file : files) {
            if(file.isFile()) result.add(file);
            else result.addAll(getFilesRecursively(file));
        }

        return result;
    }

    private static void unzip(File destination, File zip, AssetsSource.CopyBehaviour behaviour) {
        try {
            byte[] buffer = new byte[4096];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zip.getPath()));
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                File output = fileFromEntry(destination, entry);
                if (entry.isDirectory()) {
                    if (!output.isDirectory() && !output.mkdirs())
                        throw new IOException("Failed to create directory " + output);
                    entry = zis.getNextEntry();
                    continue;
                }
                else if(output.exists()) {
                    switch (behaviour) {
                        case SKIP, MERGE_OR_SKIP -> {
                            continue;
                        }
                        default -> {}
                    }
                }

                File parent = output.getParentFile();
                if (!parent.isDirectory() && !parent.mkdirs()) {
                    throw new IOException("Failed to create directory " + parent);
                }

                FileOutputStream fos = new FileOutputStream(output);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static File fileFromEntry(File destination, ZipEntry entry) throws IOException {
        File destFile = new File(destination, entry.getName());
        if (!destFile.getCanonicalPath().startsWith(destination.getCanonicalPath() + File.separator))
            throw new IOException("Entry is outside of the target dir: " + entry.getName());
        return destFile;
    }

}
