package su.hitori.pack.type;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import su.hitori.api.Pair;
import su.hitori.api.util.FileUtil;
import su.hitori.api.util.JSONUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public record Translations(Key key, Map<Locale, Map<String, String>> locales) implements Keyed {

    public static Translations readFolder(Key key, File languagesFolder) {
        if(!languagesFolder.exists() || languagesFolder.isDirectory()) return new Translations(key, Map.of());

        File[] files = languagesFolder.listFiles();
        assert files != null;

        Map<Locale, Map<String, String>> locales = new HashMap<>();
        for (File file : files) {
            Pair<String, String> name = FileUtil.getNameAndExtension(file);
            if(!"json".equalsIgnoreCase(name.second())) continue;

            Locale locale;
            try {
                locale = Locale.valueOf(name.first());
            }
            catch (Exception e) {continue;}

            Map<String, String> translations = new HashMap<>();
            JSONObject object = JSONUtil.readFile(file);
            for (String key1 : object.keySet()) {
                translations.put(
                        key1,
                        object.getString(key1)
                );
            }

            locales.put(locale, translations);
        }

        return new Translations(key, locales);
    }

    @Override
    public @NotNull Key key() {
        return key;
    }

    @SuppressWarnings("SpellCheckingInspection")
    public enum Locale {

        af_za, ar_sa, ast_es, az_az,
        ba_ru, bar, be_by, be_latn, bg_bg, br_fr, brb, bs_ba,
        ca_es, cs_cz, cy_gb,
        da_dk, de_at, de_ch, de_de,
        el_gr, en_au, en_ca, en_gb, en_nz, en_pt, en_ud, en_us, enp, enws, eo_uy, es_ar, es_cl, es_ec, es_es, es_mx, es_uy, es_ve, esan, et_ee, eu_es,
        fa_ir, fi_fi, fil_ph, fr_ca, fr_fr, fra_de, fur_it, fy_nl,
        ga_ie, gd_gb, gl_es,
        hal_ua, haw_us, he_il, hi_in, hn_no, hr_hr, hu_hu, hy_am,
        id_id, ig_ng, io_en, is_is, isv, it_it,
        ja_jp, jbo_en,
        ka_ge, kk_kz, kn_in, ko_kr, ksh, kw_gb, ky_kg,
        la_la, lb_lu, li_li, lmo, lo_la, lol_us, lt_lt, lv_lv, lzh,
        mk_mk, mn_mn, ms_my, mt_mt,
        nah, nds_de, nl_be, nl_nl, nn_no, no_no,
        oc_fr, ovd,
        pl_pl, pls, pt_br, pt_pt,
        qcb_es, qid, qya_aa,
        ro_ro, rpr, ru_ru, ry_ua,
        sah_sah, se_no, sk_sk, sl_si, so_so, sq_al, sr_cs, sr_sp, sv_se, sxu, szl,
        ta_in, tl_ph, tlh_aa, tok, tr_tr, tt_ru, tzo_mx,
        uk_ua,
        val_es, vec_it, vi_vn, vp_vl,
        yi_de, yo_ng, zh_cn, zh_hk, zlm_arab

    }

}
