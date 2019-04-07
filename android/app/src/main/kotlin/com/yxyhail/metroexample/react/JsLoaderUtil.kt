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

package com.yxyhail.metroexample.react

import android.app.Application
import android.text.TextUtils
import android.util.SparseArray

import com.facebook.react.ReactApplication
import com.facebook.react.ReactInstanceManager
import com.facebook.react.bridge.CatalystInstance
import com.facebook.react.bridge.ReactContext

import java.io.File
import java.util.ArrayList

object JsLoaderUtil {
    val jsState = JsState()

    @JvmOverloads
    fun load(application: Application, callBack: () -> Unit = {}) {
        if (jsState.isDev) {
            callBack()
            return
        }
        createReactContext(application) {
            loadBundle(application)
            callBack()
        }
    }


    private fun createReactContext(application: Application, callBack: () -> Unit = {}) {
        if (jsState.isDev) {
            callBack()
            return
        }
        val manager = getReactIM(application)
        if (!manager!!.hasStartedCreatingInitialContext()) {
            manager.addReactInstanceEventListener(object : ReactInstanceManager.ReactInstanceEventListener {
                override fun onReactContextInitialized(context: ReactContext) {
                    callBack()
                    manager.removeReactInstanceEventListener(this)
                }
            })
            manager.createReactContextInBackground()
        } else {
            callBack()
        }
    }

    private fun getReactIM(application: Application): ReactInstanceManager? {
        return (application as ReactApplication).reactNativeHost.reactInstanceManager
    }

    private fun getCatalyst(application: Application): CatalystInstance? {
        val manager = getReactIM(application) ?: return null
        val context = manager.currentReactContext ?: return null
        return context.catalystInstance
    }

    private fun loadBundle(application: Application) {
        if (jsState.neededBundle.size > 0) {
            for (i in jsState.neededBundle.indices) {
                val name = jsState.neededBundle[i] + jsState.bundleSuffix
                val file = File(jsState.filePath + name)
                if (file.exists() && jsState.isFilePrior) {
                    fromFile(application, name, file.absolutePath)
                } else {
                    fromAssets(application, name)
                }
            }
        } else {
            val name = jsState.bundleName + jsState.bundleSuffix
            val file = File(jsState.filePath + File.separator + name)
            if (file.exists() && jsState.isFilePrior) {
                fromFile(application, name, file.absolutePath)
            } else {
                fromAssets(application, name)
            }
        }

    }

    @JvmOverloads
    fun fromAssets(application: Application, bundleName: String, isSync: Boolean = jsState.isSync) {
        if (hasBundle(bundleName) || JsLoaderUtil.jsState.isDev) {
            return
        }
        var source = bundleName
        if (!bundleName.startsWith("assets://")) {
            source = "assets://$bundleName"
        }
        if (!source.endsWith(jsState.bundleSuffix)) {
            source = source + jsState.bundleSuffix
        }
        val catalyst = getCatalyst(application)
        try {
            if (catalyst != null) {
                catalyst.loadScriptFromAssets(application.assets, source, isSync)
                addBundle(bundleName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @JvmOverloads
    fun fromFile(application: Application, bundleName: String, sourceUrl: String, isSync: Boolean = jsState.isSync) {
        if (hasBundle(sourceUrl) || JsLoaderUtil.jsState.isDev) {
            return
        }
        if (TextUtils.isEmpty(jsState.filePath)) {
            setDefaultFileDir(application)
        }
        val bundleDir = jsState.filePath

        val catalyst = getCatalyst(application)
        try {
            if (catalyst != null) {
                catalyst.loadScriptFromFile(bundleDir + File.separator + bundleName, sourceUrl, isSync)
                addBundle(sourceUrl)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun getBasicBundle(application: Application, bundleName: String): String? {
        return if (!jsState.isFilePrior) {
            null
        } else {
            val path = getReactDir(application) + bundleName
            if (File(path).exists()) {
                path
            } else {
                null
            }
        }
    }

    private fun getReactDir(application: Application): String {
        return application.filesDir.toString() + File.separator + jsState.reactDir + File.separator
    }

    fun setDefaultFileDir(application: Application) {
        val scriptFile = File(getReactDir(application))
        jsState.filePath = scriptFile.absolutePath
    }

    fun addBundles(application: Application, bundlesName: List<String>, callBack: () -> Unit = {}) {
        jsState.neededBundle.addAll(bundlesName)
        load(application, callBack)
    }


    private fun addBundle(bundle: String) {
        val index = jsState.loadedBundle.size()
        jsState.loadedBundle.put(index, bundle)
    }

    private fun hasBundle(bundle: String): Boolean {
        return jsState.loadedBundle.indexOfValue(bundle) != -1
    }

    fun clearBundle() {
        jsState.loadedBundle.clear()
    }

    fun setJsBundle(bundleName: String, componentName: String) {
        jsState.bundleName = bundleName
        jsState.componentName = componentName
    }

    class JsState {
        var isDev = false
        internal var isSync = false
        var isFilePrior = true
        var filePath = ""
        var reactDir = "bundle"
        var bundleSuffix = ".android.bundle"
        var bundleName = ""
        var componentName = ""
        var index = 1

        val loadedBundle = SparseArray<String>()
        var neededBundle: MutableList<String> = ArrayList()
    }

}
