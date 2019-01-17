package com.mafdy.onthemove;

import android.content.Intent;

import com.facebook.stetho.Stetho;
import com.mafdy.onthemove.utils.Preferencemanager;
import com.mafdy.onthemove.utils.Utils;

/**
 * Created by SBP on 7/10/2018.
 */

public class Application extends android.app.Application {

    public Application() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .build());


        }


    }
}
