package org.seokhwan.findcoinsingroom

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionCheck(val permissionActivity: Activity, val requirePermissions:Array<String>) {
    private val permissionRequestCode = 100

    public fun permissioinCheck(){
        var failRequestPermissionList = ArrayList<String>()

        for (permission in requirePermissions){
            if (ContextCompat.checkSelfPermission(permissionActivity.applicationContext,permission) != PackageManager.PERMISSION_GRANTED){
                failRequestPermissionList.add(permission)
            }
        }

        if(failRequestPermissionList.isNotEmpty()){
            val array = arrayOfNulls<String>(failRequestPermissionList.size)
            ActivityCompat.requestPermissions(permissionActivity,failRequestPermissionList.toArray(array),permissionRequestCode)
        }
    }
}