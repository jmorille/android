package eu.ttbox.velib.ui;

import android.graphics.Canvas;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu.CanvasTransformer;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityBase;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingActivityHelper;

import eu.ttbox.velib.R;

public class CityLibSlidingMenuFragmentActivity extends SherlockFragmentActivity implements SlidingActivityBase {

    // ===========================================================
    // Sliding fragment Activity Copy
    // ===========================================================

    private SlidingActivityHelper mHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new SlidingActivityHelper(this);
        mHelper.onCreate(savedInstanceState);
        // customize the SlidingMenu

        if (findViewById(R.id.menu_frame) == null) {
            setBehindContentView(R.layout.slidingmenu_frame);
            SlidingMenu slidingMenu = customizeSlidingMenu();
            // Add selector
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            // TODO Switch the comment
            setBehindContentView(R.layout.slidingmenu_frame);
            SlidingMenu slidingMenu = customizeSlidingMenu();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // TODO add a dummy view
            // View v = new View(this);
            // setBehindContentView(v);
            // SlidingMenu slidingMenu = customizeSlidingMenu();
            // slidingMenu.setSlidingEnabled(false);
            // slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
        }

    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mHelper.onPostCreate(savedInstanceState);
    }

    @Override
    public View findViewById(int id) {
        View v = super.findViewById(id);
        if (v != null)
            return v;
        return mHelper.findViewById(id);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mHelper.onSaveInstanceState(outState);
    }

    @Override
    public void setContentView(int id) {
        setContentView(getLayoutInflater().inflate(id, null));
    }

    @Override
    public void setContentView(View v) {
        setContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setContentView(View v, LayoutParams params) {
        super.setContentView(v, params);
        mHelper.registerAboveContentView(v, params);
    }

    public void setBehindContentView(int id) {
        setBehindContentView(getLayoutInflater().inflate(id, null));
    }

    public void setBehindContentView(View v) {
        setBehindContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public void setBehindContentView(View v, LayoutParams params) {
        mHelper.setBehindContentView(v, params);
    }

    public SlidingMenu getSlidingMenu() {
        return mHelper.getSlidingMenu();
    }

    public void toggle() {
        mHelper.toggle();
    }

    public void showContent() {
        mHelper.showContent();
    }

    public void showMenu() {
        mHelper.showMenu();
    }

    public void showSecondaryMenu() {
        mHelper.showSecondaryMenu();
    }

    public void setSlidingActionBarEnabled(boolean b) {
        mHelper.setSlidingActionBarEnabled(b);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean b = mHelper.onKeyUp(keyCode, event);
        if (b)
            return b;
        return super.onKeyUp(keyCode, event);
    }

    // ===========================================================
    // Menu Overide
    // ===========================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            toggle();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
 
    
    public SlidingMenu customizeSlidingMenu() {

        SlidingMenu slidingMenu = getSlidingMenu();

        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        slidingMenu.setShadowWidthRes(R.dimen.slidingmenu_shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setBehindScrollScale(0.35f);
        slidingMenu.setSlidingEnabled(true);

        slidingMenu.setMenu(R.layout.slidingmenu_menu);
        slidingMenu.setSelectorEnabled(true);
        // slidingMenu.setSelectorDrawable(R.drawable.slidingmenu_selector);

        slidingMenu.setBehindCanvasTransformer(smTransformer);
        return slidingMenu;
    }
    
    // ===========================================================
    // Sliding Annimation
    // ===========================================================

    private static CanvasTransformer smTransformer = new CanvasTransformer() {
        @Override
        public void transformCanvas(Canvas canvas, float percentOpen) {
            float scale = (float) (percentOpen * 0.25 + 0.75);
            canvas.scale(scale, scale, canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
    };
}
