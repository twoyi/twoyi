/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi;

import android.content.Context;
import android.content.Intent;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import io.twoyi.utils.LogEvents;

/**
 * @author weishu
 * @date 2021/10/27.
 */

public class TwoyiStatusManager {

    private static final TwoyiStatusManager INSTANCE = new TwoyiStatusManager();
    private TwoyiStatusManager() {
    }

    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final AtomicBoolean mShown = new AtomicBoolean(false);

    private final CyclicBarrier mBootLatch = new CyclicBarrier(2);

    public static TwoyiStatusManager getInstance() {
        return INSTANCE;
    }

    public void updateVisibility(boolean visible) {
        mShown.set(visible);
    }

    public void markStarted() {
        if (mStarted.compareAndSet(false, true)) {
            try {
                mBootLatch.await();
            } catch (BrokenBarrierException | InterruptedException e) {
                LogEvents.trackError(e);
            }
        }
    }

    public boolean isStarted() {
        return mStarted.get();
    }

    public void reset() {
        mStarted.set(false);
        mBootLatch.reset();
    }

    public boolean waitBoot(long timeout, TimeUnit unit) throws InterruptedException, BrokenBarrierException {
        try {
            mBootLatch.await(timeout, unit);
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    public void switchOs(Context context) {
        if (!mStarted.get()) {
            return;
        }

        Intent intent;
        if (mShown.get()) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
        } else {
            intent = new Intent(context, Render2Activity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        context.startActivity(intent);
        mShown.set(!mShown.get());
    }
}
