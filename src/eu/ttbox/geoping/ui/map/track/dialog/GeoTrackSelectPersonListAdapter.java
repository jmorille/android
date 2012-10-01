package eu.ttbox.geoping.ui.map.track.dialog;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay;

public class GeoTrackSelectPersonListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private static final String TAG = "GeoTrackSelectPersonListAdapter";

    private PersonHelper helper;

    private boolean isNotBinding = true;
    private HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser;

    private final OnActivatedPersonListener mCallBack;

    public interface OnActivatedPersonListener {
        void onDoRemovePerson(Person person);

        void onDoAddPerson(Person person);
    }

    public GeoTrackSelectPersonListAdapter(Context context, Cursor c, int flags, OnActivatedPersonListener mCallBack, HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser) {
        super(context, R.layout.map_geotrack_select_dialog_list_item, c, flags); // if
                                                                                 // >10
                                                                                 // add
                                                                                 // ", flags"
        this.geoTrackOverlayByUser = geoTrackOverlayByUser;
        this.mCallBack = mCallBack;
    }

    private void intViewBinding(View view, Context context, Cursor cursor) {
        // Init Cursor
        helper = new PersonHelper().initWrapper(cursor);
        isNotBinding = false;
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        if (isNotBinding) {
            intViewBinding(view, context, cursor);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // Bind Value
        helper.setTextPersonName(holder.nameText, cursor)//
                .setTextPersonPhone(holder.phoneText, cursor);
        // Button
        final Person person = helper.getEntity(cursor);
        final String phoneNumber = helper.getPersonPhone(cursor);
        // Display Status
        boolean isActif = geoTrackOverlayByUser.containsKey(phoneNumber);
        holder.selectedSelector.setChecked(isActif);
        // Color
        int color = helper.getPersonColor(cursor);
        int colorBackground = Color.WHITE;
        int colorFocus = Color.argb(50, Color.red(color), Color.green(color), Color.blue(color));
        RoundRectShape rs = new RoundRectShape(new float[] { 10, 10, 10, 10, 10, 10, 10, 10 }, null, null);
        ShapeDrawable sdOff = new CustomShapeDrawable(rs, colorBackground, color, 10);
        ShapeDrawable sdOn = new CustomShapeDrawable(rs, colorBackground, colorFocus, 10);

        StateListDrawable stld = new StateListDrawable();
        stld.addState(new int[] { android.R.attr.state_enabled }, sdOff);
        stld.addState(new int[] { android.R.attr.state_pressed }, sdOn);

        view.setBackgroundDrawable(stld);
        // Button
        holder.selectedSelector.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mCallBack != null) {
                    ToggleButton tb = (ToggleButton) v;
                    if (tb.isChecked()) {
                        mCallBack.onDoAddPerson(person);
                    } else {
                        mCallBack.onDoRemovePerson(person);
                    }
                }

            }
        });
        // Action
        holder.pingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startService(Intents.sendSmsGeoPingRequest(context, phoneNumber));
            }
        });
        // view.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // context.startActivity(Intents.editPersone(context, entityId));
        // }
        // });

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.nameText = (TextView) view.findViewById(R.id.person_list_item_name);
        holder.phoneText = (TextView) view.findViewById(R.id.person_list_item_phone);
        holder.pingButton = (Button) view.findViewById(R.id.person_list_item_geoping_button);
        holder.selectedSelector = (ToggleButton) view.findViewById(R.id.person_list_item_status_selected);
        // Backgroup Shape

        // normal.getPaint().setShader(shader);
        // normal.setPadding(7, 3, 7, 5);
        // and store it inside the layout.
        view.setTag(holder);
        return view;
    }

    static class ViewHolder {
        Button pingButton;
        TextView nameText;
        TextView phoneText;
        ToggleButton selectedSelector;
        // CustomShapeDrawable backgroud;
    }

    /**
     * {link http://www.betaful.com/2012/01/programmatic-shapes-in-android/}
     * 
     * @author jmorille
     * 
     */
    public class CustomShapeDrawable extends ShapeDrawable {
        Paint fillpaint;
        Paint strokepaint;
        private static final int WIDTH = 5;
        private int strokeWidth;

        public CustomShapeDrawable(Shape s, int fill, int stroke, int strokeWidth) {
            super(s);
            this.strokeWidth = strokeWidth;
            fillpaint = new Paint(this.getPaint());
            fillpaint.setColor(fill);
            strokepaint = new Paint(fillpaint);
            strokepaint.setStyle(Paint.Style.STROKE);
            strokepaint.setStrokeWidth(strokeWidth);
            strokepaint.setColor(stroke);
        }

        protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
            // V1
            shape.draw(canvas, fillpaint);
            shape.draw(canvas, strokepaint);

            // V2
            // shape.resize(canvas.getClipBounds().right,
            // canvas.getClipBounds().bottom);
            // shape.draw(canvas, fillpaint);
            //
            // Matrix matrix = new Matrix();
            // matrix.setRectToRect(new RectF(0, 0,
            // canvas.getClipBounds().right, canvas.getClipBounds().bottom), new
            // RectF(strokeWidth / 2, strokeWidth / 2,
            // canvas.getClipBounds().right - strokeWidth
            // / 2, canvas.getClipBounds().bottom - strokeWidth / 2),
            // Matrix.ScaleToFit.FILL);
            // canvas.concat(matrix);
            //
            // shape.draw(canvas, strokepaint);
        }

        public void setFillColour(int c) {
            fillpaint.setColor(c);
        }

        public void setStrokeColour(int c) {
            strokepaint.setColor(c);
        }

    }

}
