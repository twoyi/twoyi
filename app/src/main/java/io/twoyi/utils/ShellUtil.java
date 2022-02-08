/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.twoyi.utils;

import com.topjohnwu.superuser.Shell;

/**
 * @author weishu
 * @date 2022/1/4.
 */

public final class ShellUtil {

    private ShellUtil() {
    }

    public static Shell newSh() {
        return Shell.Builder.create()
                .setFlags(Shell.FLAG_NON_ROOT_SHELL)
                .build("sh");
    }
}
