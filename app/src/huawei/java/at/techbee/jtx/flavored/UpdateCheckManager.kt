/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.mutableStateOf
import com.huawei.hms.jos.JosApps
import com.huawei.updatesdk.service.appmgr.bean.ApkUpgradeInfo
import com.huawei.updatesdk.service.otaupdate.CheckUpdateCallBack
import com.huawei.updatesdk.service.otaupdate.UpdateKey


class UpdateCheckManager(val activity: Activity) : UpdateCheckManagerDefinition {

    override var isForcedUpdateAvailable = mutableStateOf(false)

    override fun checkForUpdates() {
        val client = JosApps.getAppUpdateClient(activity)
        client.checkAppUpdate(activity, object: CheckUpdateCallBack {
            override fun onUpdateInfo(intent: Intent?) {
                if (intent != null) {
                    val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getSerializableExtra(UpdateKey.INFO, ApkUpgradeInfo::class.java)
                    } else {
                        intent.getSerializableExtra(UpdateKey.INFO) as ApkUpgradeInfo
                    }
                    if(intent.getBooleanExtra(UpdateKey.MUST_UPDATE, false))
                        JosApps.getAppUpdateClient(activity).showUpdateDialog(activity, info, false)
                }
            }
            override fun onMarketInstallInfo(p0: Intent?) { }
            override fun onMarketStoreError(p0: Int) { }
            override fun onUpdateStoreError(p0: Int) { }
        })
    }

}