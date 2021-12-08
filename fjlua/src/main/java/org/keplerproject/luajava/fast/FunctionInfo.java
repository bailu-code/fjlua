package org.keplerproject.luajava.fast;

/**
 * 可以被lua调用的java方法信息
 *
 * @author wl
 * @version 2021.11.23
 */
public class FunctionInfo<T> {
    /**
     * 方法名
     */
    private final String functionName;
    /**
     * 方法执行函数
     */
    private final JavaFunction<T> function;
    /**
     * 方法id，再lua中调用的id
     */
    private int funcId;

    public FunctionInfo(String functionName, JavaFunction<T> function) {
        this.functionName = functionName;
        this.function = function;
    }

    /**
     * 执行方法
     *
     * @param luaState
     * @param t
     * @return
     * @throws LuaException
     */
    public int execute(LuaState luaState, T t) throws LuaException {
        return function.execute(luaState, t);
    }

    /**
     * 是否为静态方法
     *
     * @return
     */
    public boolean isStaticFunc() {
        return function instanceof JavaStaticFunction;
    }

    public String getFunctionName() {
        return functionName;
    }

    public int getFuncId() {
        return funcId;
    }

    void setFuncId(int funcId) {
        this.funcId = funcId;
    }
}
