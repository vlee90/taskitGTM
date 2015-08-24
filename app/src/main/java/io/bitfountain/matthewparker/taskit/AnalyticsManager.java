package io.bitfountain.matthewparker.taskit;

import android.content.Context;
import android.provider.ContactsContract;
import android.util.ArrayMap;
import android.util.Log;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tagmanager.Container;
import com.google.android.gms.tagmanager.ContainerHolder;
import com.google.android.gms.tagmanager.DataLayer;
import com.google.android.gms.tagmanager.TagManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Created by vincentlee on 8/24/15.
 */

public class AnalyticsManager {
    private static final String TAG = "AnalyticsManager";
    private static final String CONTAINER_ID = "GTM-KQDB98";
    private static final int GTM_TIMEOUT = 2;
    private static final String DEBUG_TRACKING_ID =  "UA-60094916-5";

    private static AnalyticsManager sAnalyticsManager;
    private static ContainerHolder sContainerHolder;
    private static Container sContainer;
    private static TagManager sTagManager;
    private static ArrayList<Map<String, Object>> storedHits = new ArrayList<>();

    public static AnalyticsManager getInstance() {
        if (sAnalyticsManager == null) {
            sAnalyticsManager = new AnalyticsManager();
        }
        return sAnalyticsManager;
    }

    public static void startAnalyticsManager(Context context) {
        if (sTagManager == null) {
            sTagManager = TagManager.getInstance(context);

            PendingResult<ContainerHolder> pendingResult = sTagManager.loadContainerPreferNonDefault(CONTAINER_ID, R.raw.gtm_kqdb98);
            pendingResult.setResultCallback(new ResultCallback<ContainerHolder>() {
                @Override
                public void onResult(ContainerHolder containerHolder) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "ContainerHolder Status = " + containerHolder.getStatus().toString());
                    }
                    sContainerHolder = containerHolder;
                    sContainer = containerHolder.getContainer();

                    if (!containerHolder.getStatus().isSuccess()) {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "Container load is not successful");
                        }
                        return;
                    }

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Debug Build");
                        Log.d(TAG, "Refresh Requested");
                        sContainerHolder.refresh();

                        Map<String, Object> hit = DataLayer.mapOf("ga-tracking-id", DEBUG_TRACKING_ID, "isDebugMode", "true");
                        storedHits.add(0, hit);

                    }

                    pushStoredHits();

                    containerHolder.setContainerAvailableListener(new ContainerHolder.ContainerAvailableListener() {
                        @Override
                        public void onContainerAvailable(ContainerHolder containerHolder, String s) {
                            if (BuildConfig.DEBUG) {
                                Log.d(TAG, "onContainerAvailable called. Container Version = " + s);
                            }
                            sContainer = containerHolder.getContainer();
                        }
                    });
                }
            }, GTM_TIMEOUT, TimeUnit.SECONDS);
        }
    }

    public void pushEvent(String eventName, Map<String, Object> update) {
        HashMap<String, Object> map = new HashMap<>();
        update.put("event", eventName);
        filterEvent(update);
    }

    private void filterEvent(Map<String, Object> update) {
        if (sContainerHolder == null) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Batching Early Hit" + "\n" + update.toString());
            }
            storedHits.add(update);
        }
        else {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Pushing" + "\n" + update.toString());
            }
            sTagManager.getDataLayer().push(update);
        }
    }

    private static void pushStoredHits() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Dispatching Stored Hits: " + storedHits.toString());
        }
        for(Map<String, Object> hit : storedHits) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Pushing Early Hit" + hit);
            }
            DataLayer dataLayer = sTagManager.getDataLayer();
            dataLayer.push(hit);
        }
    }


}
