package su.hitori.pack.impl;

import net.kyori.adventure.key.Key;
import org.json.JSONArray;
import org.json.JSONObject;
import su.hitori.api.registry.RegistryKey;
import su.hitori.api.util.FileUtil;
import su.hitori.api.util.IOUtil;
import su.hitori.api.util.JSONUtil;
import su.hitori.pack.PackModule;
import su.hitori.pack.generation.ErrorStack;
import su.hitori.pack.generation.GenerationContext;
import su.hitori.pack.generation.supplier.GenerationSupplier;
import su.hitori.pack.type.glyph.Glyph;
import su.hitori.pack.type.glyph.GlyphSnapshot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GlyphConveyor extends AbstractConveyorWithRegistry<GlyphSnapshot, Glyph> {

    private static final int START = 0xE100;
    private static final int MAX_INDEX = 6400;
    public static final int[] SHIFTS = {
            1, 2, 4, 8, 16, 32, 64, 128, 256, 512
    };

    public GlyphConveyor(PackModule packModule, Key key, RegistryKey<Glyph> registryKey) {
        super(key, registryKey);
        createShiftGlyphs(packModule);
    }

    private void createShiftGlyphs(PackModule packModule) {
        byte[] texture;

        try (InputStream is = packModule.getResourceAsStream("textures/null.png")) {
            texture = IOUtil.readInputStream(is);
        }
        catch (Throwable e) {
            e.printStackTrace();
            return;
        }

        int index = registry.elements().size();
        for (int i = 0; i < 2; i++) {
            for (int shift : SHIFTS) {
                Glyph glyph = shiftGlyph(i == 1, texture, shift, START + ++index);
                registry.register(glyph.key(), glyph);
            }
        }
    }

    private Glyph shiftGlyph(boolean negative, byte[] texture, int shift, int index) {
        return new Glyph(
                new GlyphSnapshot(
                        Key.key((negative ? "neg_shift_" : "shift_") + shift),
                        texture,
                        "misc/null",
                        -32768,
                        (negative ? -shift : shift) - 2
                ),
                index,
                false
        );
    }

    @Override
    public void collect(ErrorStack errorStack) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        if(!toAdd.isEmpty()) {
            for (GenerationSupplier<GlyphSnapshot> supplier : toAdd) {
                suppliers.addLast(supplier.key(), supplier);
            }
            toAdd.clear();
        }

        for (Glyph element : registry.elements()) {
            if(element.temp()) registry.remove(element.key());
        }

        int index = registry.elements().size();
        List<GlyphSnapshot> glyphSnapshots = new ArrayList<>();
        suppliers.forEach(supplier -> {
            try {
                glyphSnapshots.addAll(supplier.supply());
            }
            catch (Throwable ex) {
                ex.printStackTrace();
            }
        });

        for (GlyphSnapshot glyphSnapshot : glyphSnapshots) {
            if(index >= MAX_INDEX) break;
            registry.register(glyphSnapshot.key(), new Glyph(glyphSnapshot, START + ++index, true));
        }

        collectingOrGenerating = false;
    }

    @Override
    public void generate(GenerationContext context) {
        if(collectingOrGenerating) return;
        collectingOrGenerating = true;

        File fontFile = new File(context.folder(), "assets/minecraft/font/default.json");
        fontFile.getParentFile().mkdirs();

        JSONObject fontBody = JSONUtil.readFile(fontFile);
        JSONArray providers;
        if(fontBody.isNull("providers")) providers = new JSONArray();
        else providers = fontBody.getJSONArray("providers");

        File textures = new File(context.folder(), "assets/minecraft/textures/font/");
        for (Glyph glyph : registry.elements()) {
            GlyphSnapshot glyphSnapshot = glyph.glyphSnapshot();

            byte[] texture = glyphSnapshot.texture();
            if(texture == null) {
                context.errorStack().add("Текстура для глифа не существует", glyphSnapshot.key().asMinimalString());
                continue;
            }

            File output = new File(textures, glyphSnapshot.path() + ".png");
            output.getParentFile().mkdirs();
            if(output.length() != texture.length) {
                try (FileOutputStream fos = new FileOutputStream(output)) {
                    fos.write(texture);
                    fos.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
            }

            providers.put(
                    new JSONObject()
                            .put("chars", Collections.singletonList(glyph.getSymbol()))
                            .put("file", "font/" + glyphSnapshot.path() + ".png")
                            .put("ascent", glyphSnapshot.ascent())
                            .put("height", glyphSnapshot.height())
                            .put("type", "bitmap")
            );
        }

        JSONObject result = new JSONObject();
        result.put("providers", providers);
        FileUtil.writeTextToFile(fontFile, result.toString());

        collectingOrGenerating = false;
    }

}
