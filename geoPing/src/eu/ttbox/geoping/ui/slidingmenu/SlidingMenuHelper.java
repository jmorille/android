package eu.ttbox.geoping.ui.slidingmenu;

import android.content.Context;
import android.graphics.Canvas;

import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.CanvasTransformer;

import eu.ttbox.geoping.R;

public class SlidingMenuHelper {

    public static SlidingMenu newInstanceForMap(Context context) {
        SlidingMenu slidingMenu = new SlidingMenu(context);
        return customizeSlidingInstance(context,slidingMenu,  SlidingMenu.TOUCHMODE_MARGIN);
     }
   
    public static SlidingMenu newInstance(Context context) {
          SlidingMenu slidingMenu = new SlidingMenu(context);
        return customizeSlidingInstance(context,slidingMenu,  SlidingMenu.TOUCHMODE_FULLSCREEN);
    }
    public static SlidingMenu customizeSlidingInstance(Context context, final SlidingMenu slidingMenu, int touchModeAbove) {
       
        slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(touchModeAbove);
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setBehindScrollScale(0.35f);
        slidingMenu.setSlidingEnabled(true);
        
        slidingMenu.setMenu(R.layout.slidingmenu_menu);
        slidingMenu.setSelectorEnabled(true);
        slidingMenu.setSelectorDrawable(R.drawable.slidingmenu_selector);


        slidingMenu.setBehindCanvasTransformer(smTransformer);
        return slidingMenu;
    }
    
    // ===========================================================
    // Sliding Annimation
    // ===========================================================
    
    private static  CanvasTransformer smTransformer = new CanvasTransformer() {
        @Override
        public void transformCanvas(Canvas canvas, float percentOpen) {
            float scale = (float) (percentOpen * 0.25 + 0.75);
            canvas.scale(scale, scale, canvas.getWidth() / 2, canvas.getHeight() / 2);
        }
    };
    
}
