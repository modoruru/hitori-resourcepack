package su.hitori.pack.impl;

import io.papermc.paper.ServerBuildInfo;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import su.hitori.api.Pair;
import su.hitori.api.logging.LoggerFactory;
import su.hitori.api.module.ModuleDescriptor;
import su.hitori.api.util.FileUtil;
import su.hitori.api.util.JSONUtil;
import su.hitori.api.util.Pipeline;
import su.hitori.api.util.Task;
import su.hitori.pack.PackModule;
import su.hitori.pack.generation.ErrorStack;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.generation.GenerationConveyor;
import su.hitori.pack.generation.Generator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@NotNullByDefault
public final class GeneratorImpl implements Generator {

    private static final Logger logger = LoggerFactory.instance().create(Generator.class);
    private static final Set<String> ALLOWED_FILE_EXTENSIONS = Set.of(
            "mcmeta", "json", "png", "ogg", "fsh", "vsh", "glsl"
    );

    private final PackModule packModule;
    private final File workingDirectory;
    private final Pipeline<ConveyorWrapper> conveyorsPipeline;
    private final AtomicBoolean generating;

    private boolean generateRequested;

    @Nullable
    private Pair<File, String> result;

    public GeneratorImpl(PackModule packModule, File workingDirectory) {
        this.packModule = packModule;
        this.workingDirectory = workingDirectory;
        this.conveyorsPipeline = new Pipeline<>();
        this.generating = new AtomicBoolean();
    }

    public boolean generate() {
        if(generating.get()) return false;
        if(generateRequested) return true;

        generateRequested = true;
        Task.async(this::generateInternal, 1L);
        return true;
    }

    private void generateInternal() {
        generateRequested = false;
        generating.set(true);

        long start = System.currentTimeMillis();
        logger.info("Starting pack generation");

        Set<Key> toRemove = new HashSet<>();
        for (ConveyorWrapper wrapper : conveyorsPipeline) {
            if(!wrapper.moduleDescriptor().isEnabled()) toRemove.add(wrapper.conveyor.key());
        }
        toRemove.forEach(conveyorsPipeline::remove);

        File tempFolder = new File(workingDirectory, "temp/");
        tempFolder.mkdirs();

        File[] files = tempFolder.listFiles();
        assert files != null;
        for (File file : files) {
            FileUtil.deleteRecursively(file);
        }

        logger.info("Collecting data on conveyors...");

        GenerationContext context = new GenerationContext(tempFolder, new ErrorStack());
        for (ConveyorWrapper wrapper : conveyorsPipeline) {
            GenerationConveyor<?> conveyor = wrapper.conveyor;
            try {
                conveyor.collect(context.errorStack());
            }
            catch (Throwable ex) {
                generating.set(false);
                throw new RuntimeException("problem while collecting objects in conveyor", ex);
            }
        }

        logger.info("Generating from conveyors... ");

        for (ConveyorWrapper wrapper : conveyorsPipeline) {
            GenerationConveyor<?> conveyor = wrapper.conveyor;
            try {
                conveyor.generate(context);
            }
            catch (Throwable ex) {
                generating.set(false);
                throw new RuntimeException("problem while generating pack in conveyor", ex);
            }
        }

        logger.info("Creating meta file and archiving...");
        createMetaFile(tempFolder);

        File file = createArchive(tempFolder);
        String hash = sha1AsString(file);

        result = Pair.of(file, hash);
        generating.set(false);

        logger.info(String.format("Done! Pack generated in %.3fs", (System.currentTimeMillis() - start) * 0.001));

        Bukkit.getOnlinePlayers().forEach(packModule.packServer()::sendPack);
    }

    private void createMetaFile(File folder) {
        File file = new File(folder, "pack.mcmeta");
        JSONObject object = JSONUtil.readFile(file);

        JSONObject pack;
        if(object.has("pack")) pack = object.getJSONObject("pack");
        else pack = new JSONObject().put("description", "hitori framework");

        pack.put("pack_format", switch (ServerBuildInfo.buildInfo().minecraftVersionId()) {
            case "1.21.10" -> 69;
            default -> 75.0;
        });

        object.put("pack", pack);

        FileUtil.writeTextToFile(file, object.toString());
    }

    private File createArchive(File root) {
        File zip = new File(root, "pack.zip");
        try {
            Files.createFile(zip.toPath());
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        try (RandomAccessFile raf = new RandomAccessFile(zip, "rw");
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(raf.getFD()))) {
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);

            Path folder = root.toPath();
            Set<String> seenDirectories = new HashSet<>();

            var time = FileTime.fromMillis(0);

            Files.walk(folder).forEach(path -> {
                try {
                    if (path.equals(zip.toPath())) return;

                    Path relativePath = folder.relativize(path);
                    String entryName = relativePath.toString().replace("\\", "/");

                    if (Files.isDirectory(path)) {
                        if (seenDirectories.add(entryName)) {
                            ZipEntry dirEntry = new ZipEntry(entryName.endsWith("/") ? entryName : entryName + "/");
                            dirEntry.setLastModifiedTime(time);
                            zos.putNextEntry(dirEntry);
                            zos.closeEntry();
                        }
                    }
                    else {
                        int dode = entryName.lastIndexOf('.');
                        if(dode != -1) {
                            String extension = entryName.substring(dode + 1).toLowerCase();
                            if(!ALLOWED_FILE_EXTENSIONS.contains(extension)) {
                                logger.warning("Found entry with non-allowed extension; it will not be included in final archive. Entry name: \"" + entryName + "\", Extension: \"" + extension + "\".");
                                return;
                            }
                        }

                        Path parent = relativePath.getParent();
                        if (parent != null) {
                            String dirName = parent.toString().replace("\\", "/");
                            if (seenDirectories.add(dirName)) {
                                ZipEntry dirEntry = new ZipEntry(dirName.endsWith("/") ? dirName : dirName + "/");
                                dirEntry.setLastModifiedTime(time);
                                zos.putNextEntry(dirEntry);
                                zos.closeEntry();
                            }
                        }

                        ZipEntry fileEntry = new ZipEntry(entryName);
                        fileEntry.setLastModifiedTime(time);
                        zos.putNextEntry(fileEntry);
                        Files.copy(path, zos);
                        zos.closeEntry();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            zos.finish();
        }
        catch (IOException e) {
            throw new RuntimeException("Error while creating ZIP file", e);
        }

        return zip;
    }

    private static String sha1AsString(File file) {
        try {
            // generate hash
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            InputStream fis = new FileInputStream(file);
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            fis.close();

            byte[] hash = digest.digest();

            // create string from hash
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isGenerating() {
        return generating.get();
    }

    @Override
    public <E extends Keyed> @Nullable GenerationConveyor<E> getConveyor(Key key, Class<E> objectType) throws IllegalAccessError {
        if(generating.get()) throw new IllegalAccessError("Generator is generating right now! check Generator#isGenerating() first!");
        ConveyorWrapper wrapper = conveyorsPipeline.get(key);
        if(wrapper == null) return null;

        if(!wrapper.objectType.isAssignableFrom(objectType)) throw new IllegalArgumentException(String.format(
                "Requested conveyor under \"%s\" id, but types are wrong: Present: [%s], Expected: [%s]",
                key.asString(),
                wrapper.objectType.getName(),
                objectType.getName()
        ));

        return (GenerationConveyor<E>) wrapper.conveyor;
    }

    @Override
    public <E extends Keyed> void addConveyor(ModuleDescriptor moduleDescriptor, GenerationConveyor<E> conveyor, Class<E> objectType) {
        Key key = conveyor.key();
        if(conveyorsPipeline.containsKey(key) || (!moduleDescriptor.isEnabled() && !moduleDescriptor.isEnabling())) return;
        conveyorsPipeline.addLast(key, new ConveyorWrapper(moduleDescriptor, conveyor, objectType));
    }

    @Override
    public Optional<Pair<File, String>> getResult() {
        return generating.get()
                ? Optional.empty()
                : Optional.ofNullable(result);
    }

    private record ConveyorWrapper(ModuleDescriptor moduleDescriptor, GenerationConveyor<?> conveyor, Class<?> objectType) {
    }

}
