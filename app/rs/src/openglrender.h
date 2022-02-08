// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

//
// Created by weishu on 2020/12/24.
//

#pragma once

extern int destroyOpenGLSubwindow();

extern void repaintOpenGLDisplay();

extern int setNativeWindow(void*);

extern int resetSubWindow(void* p_window, int wx, int wy, int ww, int wh, int fbw, int fbh, float dpr, float zRot);

extern int startOpenGLRenderer(void* win, int width, int height, int xdpi, int ydpi, int fps);

extern int removeSubWindow(void* );