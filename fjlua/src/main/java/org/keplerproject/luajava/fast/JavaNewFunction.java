package org.keplerproject.luajava.fast;

/**
 * 创建java对象
 *
 * @author wl
 * @version 2021.11.22
 */
public interface JavaNewFunction<T> {

    /**
     * 使用构造方法，创建java对象
     *
     * @param luaState
     * @return
     */
    T newInstance(LuaState luaState);

}
