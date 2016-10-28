package io.growing.dryad.internal.impl

import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicReference

import net.sf.cglib.proxy.{MethodInterceptor, MethodProxy}

/**
 * Component:
 * Description:
 * Date: 2016/10/28
 *
 * @author Andy Ai
 */
private[impl] class ObjectRef(val reference: AtomicReference[Any]) extends MethodInterceptor {

  override def intercept(o: scala.Any, method: Method, objects: Array[AnyRef], methodProxy: MethodProxy): AnyRef = {
    method.invoke(reference.get())
  }

}
