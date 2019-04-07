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

package com.yxyhail.metroexample.utils

import android.content.Context
import android.content.SharedPreferences

import com.yxyhail.metroexample.MainApplication

object SpConfig {

    var loadRuntime: Boolean
        get() = getBoolean("isLoadRuntime")
        set(isLoadRuntime) = saveBoolean("isLoadRuntime", isLoadRuntime)

    var index: Int
        get() = getInt("index")
        set(index) = saveInt("index", index)




    private val sp: SharedPreferences
        get() = MainApplication.app!!.getSharedPreferences("colonel_tool", Context.MODE_PRIVATE)


    private fun getString(key: String): String? {
        return sp.getString(key, "")
    }

    private fun getInt(key: String): Int {
        return sp.getInt(key, 0)
    }

    private fun getBoolean(key: String): Boolean {
        return sp.getBoolean(key, false)
    }

    private fun getLong(key: String): Long {
        return sp.getLong(key, 0)
    }

    private fun saveString(key: String, value: String) {
        sp.edit().putString(key, value).apply()
    }

    private fun saveInt(key: String, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    private fun saveBoolean(key: String, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    private fun saveLong(key: String, value: Long) {
        sp.edit().putLong(key, value).apply()
    }
}
