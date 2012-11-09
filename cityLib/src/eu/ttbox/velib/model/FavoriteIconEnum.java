package eu.ttbox.velib.model;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import eu.ttbox.velib.R;

public enum FavoriteIconEnum {

    STAR(android.R.drawable.star_big_on), //
    // HOME(R.drawable.favicon_home), //
    // COFEE(R.drawable.favicon_cofee), //
    // FLOWER(R.drawable.favicon_flower);
    home(R.drawable.favicon_home), //
    home2(R.drawable.favicon_home2), //
    home3(R.drawable.favicon_home3), //
    amor(R.drawable.favicon_amor), //
    // strawberry(R.drawable.favicon_strawberry.ico
    babelfish(R.drawable.favicon_babelfish), //
    bell(R.drawable.favicon_bell), //
    book(R.drawable.favicon_book), //
    buoy(R.drawable.favicon_buoy), //
    clock(R.drawable.favicon_clock), //
    cofee(R.drawable.favicon_cofee), //
    cofee2(R.drawable.favicon_cofee2), //
    cofee3(R.drawable.favicon_cofee3), //
    cookie(R.drawable.favicon_cookie), //
    daemons(R.drawable.favicon_daemons), //
    doctor(R.drawable.favicon_doctor), //
    doctor2(R.drawable.favicon_doctor2), //
    draw(R.drawable.favicon_draw), //
    elephan(R.drawable.favicon_elephan), //
    encrypted(R.drawable.favicon_encrypted), //
    energy(R.drawable.favicon_energy), //
    family(R.drawable.favicon_family), //
    flag(R.drawable.favicon_flag), //
    flower(R.drawable.favicon_flower), //
    flower2(R.drawable.favicon_flower2), //
    fortress(R.drawable.favicon_fortress), //
    gadu(R.drawable.favicon_gadu), //
    game(R.drawable.favicon_game), //
    game2(R.drawable.favicon_games_kids), //
    game3(R.drawable.favicon_package_toys), //
    garage(R.drawable.favicon_garage), //
    graphics(R.drawable.favicon_graphics), //
    heart(R.drawable.favicon_heart), //
    identity(R.drawable.favicon_identity), //
    katomic(R.drawable.favicon_katomic), //
    katuberling(R.drawable.favicon_katuberling), //
    keys(R.drawable.favicon_keys), //
    kwallet(R.drawable.favicon_kwallet), //
    mozillacrystal(R.drawable.favicon_mozillacrystal), //
    outbox(R.drawable.favicon_outbox1), //
    penguin(R.drawable.favicon_penguin), //
    personal(R.drawable.favicon_personal), //
    proxy(R.drawable.favicon_proxy), //
    pysol(R.drawable.favicon_pysol), //
    run(R.drawable.favicon_run), //
    run2(R.drawable.favicon_runit), //
    smiley(R.drawable.favicon_smiley), //
    smileylol(R.drawable.favicon_smiley_lol), //
    steps(R.drawable.favicon_goto), //
    testbedprotocol(R.drawable.favicon_testbed_protocol), //
    toast(R.drawable.favicon_toast), //
    toast2(R.drawable.favicon_toast2), //
    unlock(R.drawable.favicon_unlock), //
    utilities(R.drawable.favicon_utilities), //
    wifi(R.drawable.favicon_wifi), //
    wizard(R.drawable.favicon_wizard), //
    x(R.drawable.favicon_x), //
    xeyes(R.drawable.favicon_xeyes);

    public static FavoriteIconEnum DEFAULT_ICON = STAR;

    final int imageResource;

    private Bitmap cacheBitmap;

    FavoriteIconEnum(int iconId) {
        this.imageResource = iconId;
    }

    public int getImageResource() {
        return imageResource;
    }

    public Bitmap getIconBitmap(Resources resources) {
        if (cacheBitmap == null) {
            cacheBitmap = BitmapFactory.decodeResource(resources, imageResource);
        }
        return cacheBitmap;
    }

    public static FavoriteIconEnum getFromName(String name) {
        if (TextUtils.isEmpty(name)) {
            return null;
        } else {
            return FavoriteIconEnum.valueOf(name);
        }
    }

}
