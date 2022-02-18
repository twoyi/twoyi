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

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Looper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TwoyiMessenger {
    public static final String TAG = "TwoyiMessenger";

    public static final String SWITCH_HOST = "SWITCH_HOST";

    public static final String PING = "PING";

    private static final String SOCK_NAME = "TWOYI_SOCK";

    private volatile OutputStreamWriter mWriter;
    private final ReentrantReadWriteLock mLock;
    private static TwoyiMessenger INSTANCE;
    private LocalSocket socket;

    private static final Executor EXECUTOR = Executors.newCachedThreadPool();

    private TwoyiMessenger() {
        this.socket = null;
        this.mLock = new ReentrantReadWriteLock();
    }

    public TwoyiMessenger connect() {
        if (this.socket != null) {
            return this;
        }

        try {
            this.socket = new LocalSocket(LocalSocket.SOCKET_SEQPACKET);
            this.socket.connect(new LocalSocketAddress(SOCK_NAME));
            InputStream is = this.socket.getInputStream();
            OutputStream os = this.socket.getOutputStream();
            this.mWriter = new OutputStreamWriter(os);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public static synchronized TwoyiMessenger getInstance() {
        if (TwoyiMessenger.INSTANCE == null) {
            TwoyiMessenger.INSTANCE = new TwoyiMessenger();
        }

        return TwoyiMessenger.INSTANCE;
    }

    public void send(String msg) {

        if (Looper.getMainLooper() == Looper.myLooper()) {
            EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    write(msg);
                }
            });
        } else {
            this.write(msg);
        }
    }

    private void write(String msg) {
        synchronized (this) {
            if (mWriter == null) {
                connect();
            }
        }

        try {
            this.mLock.writeLock().lock();
            this.mWriter.write(msg);
            this.mWriter.flush();
        } catch (Throwable ignored) {
        } finally {
            this.mLock.writeLock().unlock();
        }
    }
}

