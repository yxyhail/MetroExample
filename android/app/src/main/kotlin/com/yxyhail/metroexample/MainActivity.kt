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

package com.yxyhail.metroexample

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.util.Arrays

import com.yxyhail.metroexample.react.JsLoaderUtil
import com.yxyhail.metroexample.ui.Business1Activity
import com.yxyhail.metroexample.ui.Business2Activity
import com.yxyhail.metroexample.ui.Business3Activity
import com.yxyhail.metroexample.ui.Business4Activity
import com.yxyhail.metroexample.ui.Common1Activity
import com.yxyhail.metroexample.ui.Common2Activity
import com.yxyhail.metroexample.utils.FileUtil
import com.yxyhail.metroexample.utils.SpConfig
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener {
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        business1.setOnClickListener(this)
        business2.setOnClickListener(this)
        business3.setOnClickListener(this)
        business4.setOnClickListener(this)
        JsLoaderUtil.jsState.isFilePrior = true

        //模拟下载Bundle到本地File
        JsLoaderUtil.setDefaultFileDir(application)
        val file = FileUtil.copyAssetsFile(this, "bundle.zip")
        FileUtil.unZip(file, filesDir.absolutePath)

        SpConfig.index = 0

        intent_to_common1.setOnClickListener(this)
        intent_to_common2.setOnClickListener(this)


        runtime_switch.isChecked = SpConfig.loadRuntime
        setCheck(SpConfig.loadRuntime)
        runtime_switch.setOnCheckedChangeListener { _, isChecked ->
            SpConfig.loadRuntime = isChecked
            setCheck(isChecked)
        }

        JsLoaderUtil.jsState.bundleName = "business3"
        JsLoaderUtil.load(application)
    }

    private fun setCheck(isChecked: Boolean) {
        runtime_text.text = if (isChecked) "LoadRuntime--on" else "LoadRuntime-off"
    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.business1 -> windowIntent(Business1Activity::class.java)
            R.id.business2 -> {
                JsLoaderUtil.jsState.isFilePrior = true
                JsLoaderUtil.setJsBundle("business2", "App2")
                windowIntent(Business2Activity::class.java)
            }
            R.id.business3 -> windowIntent(Business3Activity::class.java)
            R.id.business4 -> {
                JsLoaderUtil.setJsBundle("business4", "App4")
                JsLoaderUtil.load(application) { windowIntent(Business4Activity::class.java) }
            }
            R.id.intent_to_common1 ->
                JsLoaderUtil.addBundles(application,
                        Arrays.asList("business1", "business2", "business3", "business4")) {
                    configComponentName(Common1Activity::class.java)
                }
            R.id.intent_to_common2 -> {
                JsLoaderUtil.jsState.neededBundle.addAll(Arrays.asList("business1", "business2", "business3", "business4"))
                configComponentName(Common2Activity::class.java)
            }
            else -> {
            }
        }
    }


    private fun configComponentName(goalClass: Class<*>) {
        val text = if (goalClass == Common1Activity::class.java) {
            intent_to_common1.text.toString()
        } else {
            intent_to_common2.text.toString()
        }
        index = text.substring(text.length - 1).toInt() - 1

        if (index == 4) index = 1 else index++

        JsLoaderUtil.jsState.componentName = "App$index"
        windowIntent(goalClass)
        Handler().postDelayed({
            val show = if (index == 4) {
                1
            } else {
                index + 1
            }
            if (goalClass == Common1Activity::class.java) {
                intent_to_common1.text = "提前loadBundle后跳转到同一个页面-Business$show"
            } else {
                intent_to_common2.text = "先设置bundle名称，进入时loadBundle-Business$show"
            }
        }, 1000)
    }

    private fun windowIntent(goalClass: Class<*>) {
        val intent = Intent(this, goalClass)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        System.exit(0)
    }
}
