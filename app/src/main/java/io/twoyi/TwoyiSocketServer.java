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

/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi;

import android.content.Context;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import io.twoyi.ui.SettingsActivity;
import io.twoyi.utils.UIHelper;

/**
 * @author weishu
 * @date 2021/10/27.
 */

public class TwoyiSocketServer {

    private static final String TAG = "TwoyiSocketServer";

    private static TwoyiSocketServer INSTANCE;

    private static final String SOCK_NAME = "TWOYI_SOCK";

    private static final String SWITCH_HOST = "SWITCH_HOST";
    private static final String BOOT_COMPLETED = "BOOT_COMPLETED";

    private static final String JUMP_HOST_SETTINGS= "SETTINGS";

    private static ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    private final AtomicBoolean mStarted = new AtomicBoolean(false);
    private final Context mContext;

    private TwoyiSocketServer(Context context) {
        mContext = context;
    }

    public static TwoyiSocketServer getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new TwoyiSocketServer(context);
        }

        return INSTANCE;
    }

    public void start() {
        if (mStarted.compareAndSet(false, true)) {
            EXECUTOR.submit(this::start0);
        }
    }

    private void start0() {
        try {
            LocalSocket socket = new LocalSocket(LocalSocket.SOCKET_SEQPACKET);
            socket.bind(new LocalSocketAddress(SOCK_NAME, LocalSocketAddress.Namespace.ABSTRACT));
            LocalServerSocket localServerSocket = new LocalServerSocket(socket.getFileDescriptor());

            Thread currentThread = Thread.currentThread();
            while (!currentThread.isInterrupted()) {
                LocalSocket localSocket = localServerSocket.accept();
                handleSocket(localSocket);
            }
        } catch (IOException e) {
            Log.e(TAG, "start socket failed", e);
        }
    }

    private void handleSocket(LocalSocket socket) {
        EXECUTOR.submit(() -> handleSocket0(socket));
    }

    private void handleSocket0(LocalSocket socket) {
        try {
            InputStream inputStream = socket.getInputStream();
            Thread currentThread = Thread.currentThread();

            while (!currentThread.isInterrupted()) {
                byte[] data = new byte[1024];
                int read = inputStream.read(data);
                handleData(new String(data, 0, read, StandardCharsets.US_ASCII));
            }

        } catch (IOException ignored) {
        }
    }

    private void handleData(String msg) {
        if (msg.startsWith(SWITCH_HOST)) {
            // switch host system
            TwoyiStatusManager.getInstance().switchOs(mContext);
        } else if (msg.startsWith(BOOT_COMPLETED)) {
            // machine started
            TwoyiStatusManager.getInstance().markStarted();
        } else if (msg.startsWith(JUMP_HOST_SETTINGS)) {
            // UIHelper.startActivity(mContext, AboutActivity.class);
            UIHelper.startActivity(mContext, SettingsActivity.class);
        }
    }
}
