package catt.mvp.framework.proxy

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.view.ViewGroup
import catt.mvp.framework.adm.BaseDialogFragmentStack
import catt.mvp.framework.app.BaseDialogFragment
import catt.mvp.framework.function.component.*
import catt.mvp.framework.proxy.annotations.InjectPresenter
import catt.mvp.framework.proxy.component.ProxyAnalyticalComponent
import java.lang.ref.Reference
import java.lang.ref.WeakReference
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 泛型注释
 * type T, 需要被代理的DialogFragment
 */
abstract class ProxyBaseDialogFragment<T: BaseDialogFragment> : ILifecycle<T>, ProxyAnalyticalComponent, IDialogComponent{

    private val injectPresenter: InjectPresenter by lazy {
        this@ProxyBaseDialogFragment::class.java.getInjectPresenter()
    }

    private val presenterInstance by lazy {
        newInstancePresenter(injectPresenter) }

    private val presenterClazz by lazy {
        presenterInstance::class.java.getDeclaredPresenterClass()
    }

    private val castPresenter by lazy {
        presenterClazz.castPresenter(presenterInstance)
    }

    override fun <T> getPresenterInterface(): T = castPresenter as T

    private val viewClass: Class<out Any> by lazy {
        this@ProxyBaseDialogFragment::class.java.getDeclaredViewClass()
    }

    private var lifecycleState: Lifecycle.State = Lifecycle.State.INITIALIZED

    override val currentLifecycleState: Lifecycle.State
        get() = lifecycleState

    private val declaredClazz: Array<Type>
        get() {
            val genType = javaClass.genericSuperclass
            return (genType as ParameterizedType).actualTypeArguments
        }

    open val widthLayoutSize:Int?= ViewGroup.LayoutParams.MATCH_PARENT

    open val heightLayoutSize:Int = ViewGroup.LayoutParams.MATCH_PARENT


    private val dialog : T?
        get() = BaseDialogFragmentStack.get().search(declaredClazz[0] as Class<T>)

    override val reference: Reference<T>? by lazy {
        WeakReference<T>(dialog)
    }

    override val target: T?
        get() = reference?.get()

    val activity: FragmentActivity?
        get() = target?.activity

    override val context: Context?
        get() = reference?.get()?.context

    val fragmentTransaction: FragmentTransaction?
        get() = target?.childFragmentManager?.beginTransaction()

    override fun onCreate() {}

    open fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        presenterInstance.onAttach(viewClass, this@ProxyBaseDialogFragment)
    }

    open fun onActivityCreated(savedInstanceState: Bundle?, arguments: Bundle?) {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    open fun onDestroyView() {
        presenterInstance.onDetach()
    }

    open fun onHiddenChanged(hidden: Boolean){}

    override fun onStart() {}

    override fun onResume() {}

    override fun onPause() {}

    override fun onStop() {}

    override fun onAny(owner: LifecycleOwner) {
        lifecycleState = owner.lifecycle.currentState
    }


    override fun onDestroy() {}
}