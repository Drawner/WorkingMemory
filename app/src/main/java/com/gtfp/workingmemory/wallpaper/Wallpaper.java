package com.gtfp.workingmemory.wallpaper;

import com.gtfp.workingmemory.R;

import android.app.Activity;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Drawn on 2015-07-31.
 */
public class Wallpaper {

    private final static String DIRECTORY = "imgDir";

    private final static String WALLPAPER_FILE = "wallpaper.png";

    private Activity mActivity;

    private File mImageFile;

    private WallpaperManager mWallpaperManager;

    private static boolean JUST_SET = true;



    public Wallpaper(Activity activity) {

        mActivity = activity;

        mWallpaperManager = WallpaperManager.getInstance(activity);
    }


    public boolean set() {

        boolean set = false;

        Bitmap bitmap = getWallpaperBitmap();

        if (bitmap == null) {

            return false;
        }

        try {

            bitmap = overlayWallpaperList(bitmap);

        } catch (Exception ex) {

            bitmap = null;
        }

        if (bitmap == null) {

            return false;
        }

        set = setWallpaper(bitmap);

        return set;
    }


    public Bitmap getWallpaperBitmap() {

        Bitmap wallpaper;

        try {

            // Get the system wallpaper
            wallpaper = (getWallpaperBitmapDrawable()).getBitmap();

//            DeleteSavedWallpaper();

            Bitmap savedBitmap = getSavedWallpaper();

            if (savedBitmap == null || !wallpaper.sameAs(savedBitmap)) {

                // Must save the system wallpaper for next time.
                if (!saveWallpaper(wallpaper)) {

                    wallpaper = null;
                }
            }

        } catch (NullPointerException ex) {

            wallpaper = null;
        }

        return wallpaper;
    }


    public BitmapDrawable getWallpaperBitmapDrawable() {

        BitmapDrawable wallpaper;

        try {

            wallpaper = ((BitmapDrawable) getWallpaper());

        } catch (ClassCastException ex) {

            wallpaper = null;
        }

        return wallpaper;
    }

    // Get the wallpaper first.
/*
 * Wallpaper info is not equal to null, that is if the live wallpaper
 * is set, then get the drawable image from the package for the
 * live wallpaper
 */
    public Drawable getWallpaper() {

        Drawable wallpaperDrawable = null;

        WallpaperInfo wallpaperInfo = mWallpaperManager.getWallpaperInfo();

        if (wallpaperInfo != null) {

            //Reference to the package manager instance
            PackageManager pm = mActivity.getApplicationContext().getPackageManager();

            wallpaperDrawable = wallpaperInfo.loadThumbnail(pm);

            // Else, if static wallpapers are set, then directly get the wallpaper image
        } else {

            wallpaperDrawable = mWallpaperManager.getDrawable();
        }

        return wallpaperDrawable;
    }


    // Saved image to internal storage
    private boolean saveWallpaper(Bitmap bitmapImage) {

        boolean saved = true;

        FileOutputStream fos = null;

        try {

            fos = new FileOutputStream(getWallpaperFile());

            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 90, fos);

            fos.close();

        } catch (Exception e) {

            saved = false;
        }

        return saved;
    }


    // get image from internal storage
    private Bitmap getSavedWallpaper() {

        Bitmap bitmap;

        try {

            bitmap = BitmapFactory.decodeStream(new FileInputStream(getWallpaperFile()));

        } catch (FileNotFoundException e) {

            bitmap = null;
        }

        return bitmap;
    }


    private File getWallpaperFile() {

        if (mImageFile != null) {

            return mImageFile;
        }

        ContextWrapper cw = new ContextWrapper(mActivity.getApplicationContext());

        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir(DIRECTORY, Context.MODE_PRIVATE);

        // Create image file
        mImageFile = new File(directory, WALLPAPER_FILE);

        return mImageFile;
    }


    public Bitmap overlayWallpaperList(Bitmap wallpaper) {

        RelativeLayout wallpaperList = (RelativeLayout)mActivity.getLayoutInflater().inflate(R.layout.wallpaperlist, null);

        BitmapDrawable bitmapDrawable = new BitmapDrawable(mActivity.getResources(), wallpaper);

        wallpaperList.setBackground(bitmapDrawable);

        View phoneView = mActivity.getWindow().getDecorView();

        wallpaperList.layout(phoneView.getLeft(), phoneView.getTop(), phoneView.getRight(), phoneView.getBottom());

        wallpaperList.setDrawingCacheEnabled(true);

        Bitmap bitmap = Bitmap.createBitmap(wallpaperList.getDrawingCache(true));

        wallpaperList.setDrawingCacheEnabled(false);

        return bitmap;
    }

    /*
    *
    *  Resource:
    *  wallpaperlist.xml
    *
    * */
    public boolean overlayWallpaperList1() {

        boolean overlay = true;

        View content = mActivity.findViewById(R.id.rlid);

        content.setDrawingCacheEnabled(true);

        Bitmap bitmap = content.getDrawingCache();

        String yourimagename = "";

        File file = new File("/sdcard/" + yourimagename + ".png");

        try {

            if (!file.exists()) {

                file.createNewFile();
            }

            FileOutputStream ostream = new FileOutputStream(file);

            bitmap.compress(Bitmap.CompressFormat.PNG, 90, ostream);

            ostream.close();

            content.invalidate();

        } catch (Exception e) {

            overlay = false;

        } finally {

            content.setDrawingCacheEnabled(false);
        }

        return overlay;
    }

    /*
       In your manifest file:

       <uses-permission android:name="android.permission.SET_WALLPAPER"></uses-permission>
    */
    public boolean setWallpaper(Bitmap bitmap) {

        boolean set = true;

        try {

            mWallpaperManager.setBitmap(bitmap);

        } catch (IOException ex) {

            set = false;
        }

        just.set(set);

        return set;
    }


    private boolean DeleteSavedWallpaper() {

        File wallpaper = getWallpaperFile();

        if (wallpaper == null) {
            return false;
        }

        return wallpaper.delete();
    }

    public void onDestroy() {

        mActivity = null;

        mWallpaperManager = null;
    }


    public static class just {

        public static boolean set() {

            boolean set = JUST_SET;

            JUST_SET = false;

            return set;
        }

        public static void set(boolean set) {

            JUST_SET = set;
        }
    }
}
