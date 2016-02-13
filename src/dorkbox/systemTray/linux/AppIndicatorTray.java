/*
 * Copyright 2014 dorkbox, llc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dorkbox.systemTray.linux;

import com.sun.jna.Pointer;
import dorkbox.util.jna.linux.AppIndicator;
import dorkbox.util.jna.linux.GtkSupport;

/**
 * Class for handling all system tray interactions.
 * <p/>
 * specialization for using app indicators in ubuntu unity
 * <p/>
 * Derived from
 * Lantern: https://github.com/getlantern/lantern/ Apache 2.0 License Copyright 2010 Brave New Software Project, Inc.
 */
public
class AppIndicatorTray extends GtkTypeSystemTray {
    private static final AppIndicator appindicator = AppIndicator.INSTANCE;

    private AppIndicator.AppIndicatorInstanceStruct appIndicator;
    private boolean isActive = false;

    public
    AppIndicatorTray() {
        GtkSupport.startGui();

        GtkSupport.dispatch(new Runnable() {
            @Override
            public
            void run() {
                appIndicator = appindicator.app_indicator_new(System.nanoTime() + "DBST", "",
                                                              AppIndicator.CATEGORY_APPLICATION_STATUS);
            }
        });
    }

    @Override
    public
    void shutdown() {
        GtkSupport.dispatch(new Runnable() {
            @Override
            public
            void run() {
                // this hides the indicator
                appindicator.app_indicator_set_status(appIndicator, AppIndicator.STATUS_PASSIVE);
                Pointer p = appIndicator.getPointer();
                gobject.g_object_unref(p);

                // GC it
                appIndicator = null;
            }
        });

        super.shutdown();
    }

    @Override
    protected
    void setIcon_(final String iconPath) {
        GtkSupport.dispatch(new Runnable() {
            @Override
            public
            void run() {
                appindicator.app_indicator_set_icon(appIndicator, iconPath);

                if (!isActive) {
                    isActive = true;

                    appindicator.app_indicator_set_status(appIndicator, AppIndicator.STATUS_ACTIVE);
                }
            }
        });
    }

    /**
     * MUST BE AFTER THE ITEM IS ADDED/CHANGED from the menu
     */
    protected
    void onMenuAdded(final Pointer menu) {
        GtkSupport.dispatch(new Runnable() {
            @Override
            public
            void run() {
                appindicator.app_indicator_set_menu(appIndicator, menu);
            }
        });
    }
}
