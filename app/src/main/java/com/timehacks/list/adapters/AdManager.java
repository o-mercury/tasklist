package com.timehacks.list.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.timehacks.list.R;


/**
 * Created by Redixbit on 03-11-2017.
 */

public class AdManager {
    public static boolean ENBALE_TEST = false;
    public static InterstitialAd ad;
    private static int count = 0;


    private static final String TEST_DEVICE = "49BAAF2DBEA7F42A1E2DBB8072E41F55";

    public static void setUpBanner(Activity activity) {
        if (activity.getString(R.string.showBannerAds).equals("true")) {
            AdView adView = activity.findViewById(R.id.adView);
            if (ENBALE_TEST)
                adView.loadAd(new AdRequest.Builder().addTestDevice(TEST_DEVICE).build());
            else
                adView.loadAd(new AdRequest.Builder().build());
        }
    }

    public static void setUpInterstitialAd(final Activity activity) {
        if (activity.getString(R.string.showInterstialAds).equals("true")) {
            ad = new InterstitialAd(activity);
            ad.setAdUnitId(activity.getString(R.string.AdmobFullScreenAdsID));
            loadAd(activity);
            ad.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    loadAd(activity);
                }
            });
        }
    }

    public static void showIntentAds(final Intent i, final Context c) {
        if (ad.isLoaded()) {
            ad.show();
            ad.setAdListener(new AdListener() {
                @Override
                public void onAdClosed() {
                    super.onAdClosed();
                    loadAd((Activity) c);
                    c.startActivity(i);
                }

                @Override
                public void onAdFailedToLoad(int e) {
                    c.startActivity(i);
                }
            });
        } else
            c.startActivity(i);
    }

    private static void loadAd(Activity activity) {
        if (count < 2)
            return;
        if (ENBALE_TEST)
            ad.loadAd(new AdRequest.Builder().addTestDevice(TEST_DEVICE).build());
        else
            ad.loadAd(new AdRequest.Builder().build());
    }

    public static void onBackPress(Activity activity) {

        if (count > 2) {
            if (activity.getString(R.string.showInterstialAds).equals("true")) {
                if(ad.isLoaded())
                ad.show();
            }
            count = 0;
        }
        count++;
    }


}
