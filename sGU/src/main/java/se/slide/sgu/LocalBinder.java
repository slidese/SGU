
package se.slide.sgu;

import android.os.Binder;

import java.lang.ref.WeakReference;

/**
 * A generic implementation of Binder to be used for local services
 * 
 * @author Geoff Bruckner 12th December 2009
 * @param <S> The type of the service being bound
 */
public class LocalBinder<S> extends Binder {
    private String TAG = "LocalBinder";
    private WeakReference<S> mService;

    public LocalBinder(S service) {
        MyLog.v(TAG, "LocalBinder created");
        mService = new WeakReference<S>(service);
    }

    public S getService() {
        MyLog.v(TAG, "getService() called");
        return mService.get();
    }
}
