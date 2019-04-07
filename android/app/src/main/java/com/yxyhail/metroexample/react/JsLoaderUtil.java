/*
 * Copyright 2019 yxyhail
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yxyhail.metroexample.react;

import android.app.Application;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactInstanceManager;
import com.facebook.react.bridge.CatalystInstance;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.modules.appregistry.AppRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class JsLoaderUtil {
    public static final JsState jsState = new JsState();

    public static void load(Application application) {
        load(application, null);
    }

    public static void load(Application application, ReactContextCallBack callBack) {
        if (jsState.isDev) {
            if (callBack != null) callBack.onInitialized();
            return;
        }
        createReactContext(application, () -> {
            loadBundle(application);
            if (callBack != null) callBack.onInitialized();
        });
    }


    public interface ReactContextCallBack {
        void onInitialized();
    }

    public static void createReactContext(Application application, ReactContextCallBack callBack) {
        if (jsState.isDev) {
            if(callBack != null) callBack.onInitialized();
            return;
        }
        final ReactInstanceManager manager = getReactIM(application);
        if (!manager.hasStartedCreatingInitialContext()) {
            manager.addReactInstanceEventListener(new ReactInstanceManager.ReactInstanceEventListener() {
                @Override
                public void onReactContextInitialized(ReactContext context) {
                    if (callBack != null) callBack.onInitialized();
                    manager.removeReactInstanceEventListener(this);
                }
            });
            manager.createReactContextInBackground();
        } else {
            if (callBack != null) callBack.onInitialized();
        }
    }

    public static ReactInstanceManager getReactIM(Application application) {
        return ((ReactApplication) application).getReactNativeHost().getReactInstanceManager();
    }

    @Nullable
    public static CatalystInstance getCatalyst(Application application) {
        ReactInstanceManager manager = getReactIM(application);
        if (manager == null) {
            return null;
        }
        ReactContext context = manager.getCurrentReactContext();
        if (context == null) {
            return null;
        }
        return context.getCatalystInstance();
    }

    public static void loadBundle(Application application) {
        if (jsState.neededBundle.size() > 0) {
            for (int i = 0; i < jsState.neededBundle.size(); i++) {
                String name = jsState.neededBundle.get(i) + jsState.bundleSuffix;
                File file = new File(jsState.filePath + name);
                if (file.exists() && jsState.isFilePrior) {
                    fromFile(application, name, file.getAbsolutePath());
                } else {
                    fromAssets(application, name);
                }
            }
        } else {
            String name = jsState.bundleName + jsState.bundleSuffix;
            File file = new File(jsState.filePath + File.separator + name);
            if (file.exists() && jsState.isFilePrior) {
                fromFile(application, name, file.getAbsolutePath());
            } else {
                fromAssets(application, name);
            }
        }

    }


    public static void fromAssets(Application application, String bundleName) {
        fromAssets(application, bundleName, jsState.isSync);
    }

    public static void fromAssets(Application application, String bundleName, boolean isSync) {
        if (hasBundle(bundleName) || JsLoaderUtil.jsState.isDev) {
            return;
        }
        String source = bundleName;
        if (!bundleName.startsWith("assets://")) {
            source = "assets://" + bundleName;
        }
        if (!source.endsWith(jsState.bundleSuffix)) {
            source = source + jsState.bundleSuffix;
        }
        CatalystInstance catalyst = getCatalyst(application);
        try {
            if (catalyst != null) {
                catalyst.loadScriptFromAssets(application.getAssets(), source, isSync);
                addBundle(bundleName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fromFile(Application application, String bundleName, String sourceUrl) {
        fromFile(application, bundleName, sourceUrl, jsState.isSync);
    }

    public static void fromFile(Application application, String bundleName, String sourceUrl, boolean isSync) {
        if (hasBundle(sourceUrl) || JsLoaderUtil.jsState.isDev) {
            return;
        }
        if (TextUtils.isEmpty(jsState.filePath)) {
            setDefaultFileDir(application);
        }
        String bundleDir = jsState.filePath;

        CatalystInstance catalyst = getCatalyst(application);
        try {
            if (catalyst != null) {
                catalyst.loadScriptFromFile(bundleDir + File.separator + bundleName, sourceUrl, isSync);
                addBundle(sourceUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getBasicBundle(Application application, String bundleName) {
        if (jsState.isFilePrior) {
            String path  =getReactDir(application) + bundleName;
            if(new File(path).exists()){
                return  path;
            }else{
                return null;
            }
        } else {
            return null;
        }
    }

    private static String getReactDir(Application application){
        return application.getFilesDir()+ File.separator+jsState.reactDir+File.separator;
    }

    public static void setDefaultFileDir(Application application) {
        File scriptFile = new File(getReactDir(application));
        jsState.filePath = scriptFile.getAbsolutePath();
    }

    public static void addBundles(Application application, List<String> bundlesName, ReactContextCallBack callBack) {
        jsState.neededBundle.addAll(bundlesName);
        load(application, callBack);
    }


    private static void addBundle(String bundle) {
        int index = jsState.loadedBundle.size();
        jsState.loadedBundle.put(index, bundle);
    }

    private static boolean hasBundle(String bundle) {
        return jsState.loadedBundle.indexOfValue(bundle) != -1;
    }

    public static void clearBundle() {
        jsState.loadedBundle.clear();
    }

    public static void setJsBundle(String bundleName, String componentName) {
        jsState.bundleName = bundleName;
        jsState.componentName = componentName;
    }

    public static class JsState {
        public boolean isDev = false;
        boolean isSync = false;
        public boolean isFilePrior = true;
        public String filePath = "";
        public String reactDir = "bundle";
        public String bundleSuffix = ".android.bundle";
        public String bundleName = "";
        public String componentName = "";
        public int index = 1;

        private SparseArray<String> loadedBundle = new SparseArray<>();
        public List<String> neededBundle = new ArrayList<>();
    }

}
