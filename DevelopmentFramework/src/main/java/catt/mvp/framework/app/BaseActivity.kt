package catt.mvp.framework.app

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.content.Intent
import android.os.Bundle
import android.view.View
import catt.compat.layout.app.CompatLayoutActivity
import catt.mvp.framework.function.component.IPermissionComponent
import catt.mvp.framework.function.helper.PermissionHelper
import com.umeng.analytics.MobclickAgent
import android.arch.lifecycle.LifecycleRegistry
import android.support.v4.app.DialogFragment
import android.util.Log.w
import catt.mvp.framework.adm.BaseActivityStack
import catt.mvp.framework.adm.BaseDialogFragmentStack
import catt.mvp.framework.proxy.IProxy
import catt.mvp.framework.proxy.ProxyBaseActivity
import kotlinx.android.synthetic.*


abstract class BaseActivity : CompatLayoutActivity(), IProxy, PermissionHelper.OnPermissionListener, LifecycleOwner {

    open val isEnableFullScreen :Boolean
        get() = true

    private val lifecycleRegistry:LifecycleRegistry by lazy{LifecycleRegistry(this@BaseActivity)}

    var isPaused:Boolean = false

    private val permission : IPermissionComponent by lazy { PermissionHelper(this, this) }

    abstract fun injectLayoutId():Int

    override val proxy:ProxyBaseActivity<*> by lazy {
        injectProxyImpl() as ProxyBaseActivity<*>
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    abstract val injectStyleTheme:Int

    override fun onCreate(savedInstanceState: Bundle?) {
        BaseActivityStack.get().push(this@BaseActivity)
        setTheme(injectStyleTheme)
        super.onCreate(savedInstanceState)
        lifecycleRegistry.addObserver(proxy)
        setContentView(injectLayoutId())
        permission.scan()
        window.decorView.postOnViewLoadCompleted()
        if(isEnableFullScreen){
            window.enableAutoFullScreen()
        }
    }

    override fun onGrantedPermissionCompleted() =
        proxy.onGrantedPermissionCompleted()

    override fun onRestart() {
        super.onRestart()
        proxy.onRestart()
    }

    override fun onResume() {
        super.onResume()
        try {
            isPaused = false
            MobclickAgent.onResume(this)
            if(isEnableFullScreen){
                window.setFullScreen()
            }
        } catch (ex: Exception) {
            w("BaseActivity", "", ex)
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
        MobclickAgent.onPause(this)
    }

    override fun onDestroy() {
        this.clearFindViewByIdCache()
        super.onDestroy()
        window.disableAutoFullScreen()
        BaseActivityStack.get().remove(this@BaseActivity)
        lifecycleRegistry.removeObserver(proxy)
        System.runFinalization()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        proxy.onActivityResult(requestCode, resultCode, data)
        permission.onActivityResultForPermissions(requestCode, resultCode, data)

        when(isContainDialogFragment()){
            true->{
                for (fragment in supportFragmentManager.fragments) {
                    fragment.onActivityResult(requestCode, resultCode, data)
                }
            }
            false->{
                for (fragment in supportFragmentManager.fragments) {
                    fragment.onActivityResult(requestCode, resultCode, data)
                }
                val statisticsShowingArray = BaseDialogFragmentStack.get().statisticsShowingDialog()
                for(index:Int in statisticsShowingArray.indices){
                    statisticsShowingArray[index].onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun isContainDialogFragment():Boolean{
        for(fragment in supportFragmentManager.fragments){
            if(fragment is DialogFragment){
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permission.onPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun View.postOnViewLoadCompleted() {
        post { proxy.onViewLoadCompleted() }
    }
}