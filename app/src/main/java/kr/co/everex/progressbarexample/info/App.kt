package kr.co.everex.progressbarexample.info

import android.app.Application
import android.content.Context

class App : Application() {

    init{
        instance = this
    }


    companion object {

        private var instance: App? = null
        fun getApplicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()

//        DEBUG = isDebuggable(this)
    }


//    /**
//     * get Debug Mode
//     *
//     * @param context
//     * @return
//     */
//    private fun isDebuggable(context: Context): Boolean {
//        var debuggable = false
//        val pm: PackageManager = context.packageManager
//        try {
//            val appinfo: ApplicationInfo = pm.getApplicationInfo(context.getPackageName(), 0)
//            debuggable = 0 != appinfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
//        } catch (e: PackageManager.NameNotFoundException) {
//            /* debuggable variable will remain false */
//        }
//        return debuggable
//    }


}