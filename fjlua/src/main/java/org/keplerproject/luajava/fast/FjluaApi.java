package org.keplerproject.luajava.fast;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author wl
 * @version 2021.11.17
 */
public class FjluaApi {
    private static final Logger LOG = getLogger(FjluaApi.class);

    private FjluaApi() {
    }


    /**
     * 调用指定对象指定方法
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static int callJavaFunction(int luaStateIndex, int funcId, int objIndex) throws LuaException {
//        LOG.debug("callJavaFunction funcId {}, objIndex {}", funcId, objIndex);
        LuaState luaState = LuaStateFactory.getOrThrowException(luaStateIndex);

        FunctionInfo functionInfo = luaState.getForLuaFunction(funcId);
        if (functionInfo == null) {
            throw new RuntimeException("not found functionInfo with funcId " + funcId);
        }
        if (functionInfo.isStaticFunc()) {
            //静态方法
            return functionInfo.execute(luaState, null);
        } else {
            Object object = luaState.getForLuaObjOrThrowException(objIndex);
            return functionInfo.execute(luaState, object);
        }
    }

    /**
     * 释放对象引用
     *
     * @param luaStateIndex
     * @param objIndex
     * @return
     * @throws LuaException
     */
    public static int releaseObjRef(int luaStateIndex, int objIndex) {
//        LOG.debug("releaseObjRef objIndex {}", objIndex);

        LuaState luaState = LuaStateFactory.getOrThrowException(luaStateIndex);
        luaState.releaseObj(objIndex);
        return 0;
    }

    /**
     * t[k] = v
     * t.k = v
     */
    public static int objectNewIndex(int luaStateIndex, String key) {
        return 0;
    }

    public static int callMethod(int luaStateIndex, long objIndex, String methodName) {
        LuaState luaState = LuaStateFactory.getOrThrowException(luaStateIndex);

        return 0;
    }

}
