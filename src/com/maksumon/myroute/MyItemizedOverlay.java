package com.maksumon.myroute;

import java.util.ArrayList;

import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.graphics.Point;
import android.graphics.drawable.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: MAKSumon
 * Date: 4/26/13
 * Time: 12:34 AM
 * To change this template use File | Settings | File Templates.
 */
public class MyItemizedOverlay  extends ItemizedOverlay<OverlayItem> {

    private ArrayList<OverlayItem> overlayItemList = new ArrayList<OverlayItem>();

    public MyItemizedOverlay(Drawable pDefaultMarker,ResourceProxy pResourceProxy) {
        super(pDefaultMarker, pResourceProxy);
    }

    public void addItem(GeoPoint p, String title, String snippet){
        OverlayItem newItem = new OverlayItem(title, snippet, p);
        overlayItemList.add(newItem);
        populate();
    }

    @Override
    public boolean onSnapToItem(int arg0, int arg1, Point arg2, IMapView arg3) {
        return false;
    }

    @Override
    protected OverlayItem createItem(int arg0) {
        return overlayItemList.get(arg0);
    }

    @Override
    public int size() {
        return overlayItemList.size();
    }
}
