package org.keplerproject.luajava.fast;

/**
 * @author wl
 * @version 2021.11.17
 */
public class FjluaDll {
    public static final Integer LUA_GLOBALSINDEX = -10002;
    public static final Integer LUA_REGISTRYINDEX = -10000;

    public static final Integer LUA_TNONE = -1;
    public static final Integer LUA_TNIL = 0;
    public static final Integer LUA_TBOOLEAN = 1;
    public static final Integer LUA_TLIGHTUSERDATA = 2;
    public static final Integer LUA_TNUMBER = 3;
    public static final Integer LUA_TSTRING = 4;
    public static final Integer LUA_TTABLE = 5;
    public static final Integer LUA_TFUNCTION = 6;
    public static final Integer LUA_TUSERDATA = 7;
    public static final Integer LUA_TTHREAD = 8;

    /**
     * Specifies that an unspecified (multiple) number of return arguments
     * will be returned by a call.
     */
    public static final Integer LUA_MULTRET = -1;

    /*
     * error codes for `lua_load' and `lua_pcall'
     */

    public static final Integer LUA_YIELD = 1;

    /**
     * a runtime error.
     */
    public static final Integer LUA_ERRRUN = 2;

    /**
     * syntax error during pre-compilation.
     */
    public static final Integer LUA_ERRSYNTAX = 3;

    /**
     * memory allocation error. For such errors, Lua does not call
     * the error handler function.
     */
    public static final Integer LUA_ERRMEM = 4;

    /**
     * error while running the error handler function.
     */
    public static final Integer LUA_ERRERR = 5;


    /********************* Lua Native Interface *************************/

    public synchronized static native long lua_open();

    public synchronized static native void lua_close(long ptr);

    public synchronized static native long lua_newthread(long ptr);

    // Stack manipulation
    public synchronized static native int lua_getTop(long ptr);

    public synchronized static native void lua_setTop(long ptr, int idx);

    public synchronized static native void lua_pushValue(long ptr, int idx);

    public synchronized static native void lua_remove(long ptr, int idx);

    public synchronized static native void lua_insert(long ptr, int idx);

    public synchronized static native void lua_replace(long ptr, int idx);

    public synchronized static native int lua_checkStack(long ptr, int sz);

    public synchronized static native void lua_xmove(long from, long to, int n);

    // Access functions
    public synchronized static native int lua_isNumber(long ptr, int idx);

    public synchronized static native int lua_isString(long ptr, int idx);

    public synchronized static native int lua_isCFunction(long ptr, int idx);

    public synchronized static native int lua_isUserdata(long ptr, int idx);

    public synchronized static native int lua_type(long ptr, int idx);

    public synchronized static native String lua_typeName(long ptr, int tp);

    public synchronized static native int lua_equal(long ptr, int idx1, int idx2);

    public synchronized static native int lua_rawequal(long ptr, int idx1, int idx2);

    public synchronized static native int lua_lessthan(long ptr, int idx1, int idx2);

    public synchronized static native double lua_toNumber(long ptr, int idx);

    public synchronized static native int lua_toInteger(long ptr, int idx);

    public synchronized static native int lua_toBoolean(long ptr, int idx);

    public synchronized static native String lua_toString(long ptr, int idx);

    public synchronized static native int lua_objlen(long ptr, int idx);

    public synchronized static native long lua_toThread(long ptr, int idx);

    // Push functions
    public synchronized static native void lua_pushNil(long ptr);

    public synchronized static native void lua_pushNumber(long ptr, double number);

    public synchronized static native void lua_pushInteger(long ptr, int integer);

    public synchronized static native void lua_pushString(long ptr, String str);

    public synchronized static native void lua_pushString(long ptr, byte[] bytes, int n);

    public synchronized static native void lua_pushBoolean(long ptr, int bool);

    // Get functions
    public synchronized static native void lua_getTable(long ptr, int idx);

    public synchronized static native void lua_getField(long ptr, int idx, String k);

    public synchronized static native void lua_rawGet(long ptr, int idx);

    public synchronized static native void lua_rawGetI(long ptr, int idx, int n);

    public synchronized static native void lua_createTable(long ptr, int narr, int nrec);

    public synchronized static native int lua_getMetaTable(long ptr, int idx);

    public synchronized static native void lua_getFEnv(long ptr, int idx);

    // Set functions
    public synchronized static native void lua_setTable(long ptr, int idx);

    public synchronized static native void lua_setField(long ptr, int idx, String k);

    public synchronized static native void lua_rawSet(long ptr, int idx);

    public synchronized static native void lua_rawSetI(long ptr, int idx, int n);

    public synchronized static native int lua_setMetaTable(long ptr, int idx);

    public synchronized static native int lua_setFEnv(long ptr, int idx);

    public synchronized static native void lua_call(long ptr, int nArgs, int nResults);

    public synchronized static native int lua_pcall(long ptr, int nArgs, int Results, int errFunc);

    // Coroutine Functions
    public synchronized static native int lua_yield(long ptr, int nResults);

    public synchronized static native int lua_resume(long ptr, int nargs);

    public synchronized static native int lua_status(long ptr);

    // Gargabe Collection Functions
    public static final int LUA_GCSTOP = 0;
    public static final int LUA_GCRESTART = 1;
    public static final int LUA_GCCOLLECT = 2;
    public static final int LUA_GCCOUNT = 3;
    public static final int LUA_GCCOUNTB = 4;
    public static final int LUA_GCSTEP = 5;
    public static final int LUA_GCSETPAUSE = 6;
    public static final int LUA_GCSETSTEPMUL = 7;

    public synchronized static native int lua_gc(long ptr, int what, int data);

    // Miscellaneous Functions
    public synchronized static native int lua_error(long ptr);

    public synchronized static native int lua_next(long ptr, int idx);

    public synchronized static native void lua_concat(long ptr, int n);

    // Some macros
    public synchronized static native void lua_pop(long ptr, int n);

    public synchronized static native void lua_newTable(long ptr);

    public synchronized static native int lua_strlen(long ptr, int idx);

    public synchronized static native int lua_isFunction(long ptr, int idx);

    public synchronized static native int lua_isTable(long ptr, int idx);

    public synchronized static native int lua_isNil(long ptr, int idx);

    public synchronized static native int lua_isBoolean(long ptr, int idx);

    public synchronized static native int lua_isThread(long ptr, int idx);

    public synchronized static native int lua_isNone(long ptr, int idx);

    public synchronized static native int lua_isNoneOrNil(long ptr, int idx);

    public synchronized static native void lua_setGlobal(long ptr, String name);

    public synchronized static native void lua_getGlobal(long ptr, String name);

    public synchronized static native int lua_getGcCount(long ptr);


    // LuaLibAux
    public static synchronized native int lua_LdoFile(long ptr, String fileName);

    public synchronized static native int lua_LdoString(long ptr, String string);
    //public synchronized native int lua_doBuffer(long ptr, byte[] buff, long sz, String n);

    public synchronized static native int lua_LgetMetaField(long ptr, int obj, String e);

    public synchronized static native int lua_LcallMeta(long ptr, int obj, String e);

    public synchronized static native int lua_Ltyperror(long ptr, int nArg, String tName);

    public synchronized static native int lua_LargError(long ptr, int numArg, String extraMsg);

    public synchronized static native String lua_LcheckString(long ptr, int numArg);

    public synchronized static native String lua_LoptString(long ptr, int numArg, String def);

    public synchronized static native double lua_LcheckNumber(long ptr, int numArg);

    public synchronized static native double lua_LoptNumber(long ptr, int numArg, double def);

    public synchronized static native int lua_LcheckInteger(long ptr, int numArg);

    public synchronized static native int lua_LoptInteger(long ptr, int numArg, int def);

    public synchronized static native void lua_LcheckStack(long ptr, int sz, String msg);

    public synchronized static native void lua_LcheckType(long ptr, int nArg, int t);

    public synchronized static native void lua_LcheckAny(long ptr, int nArg);

    public synchronized static native int lua_LnewMetatable(long ptr, String tName);

    public synchronized static native void lua_LgetMetatable(long ptr, String tName);

    public synchronized static native void lua_Lwhere(long ptr, int lvl);

    public synchronized static native int lua_Lref(long ptr, int t);

    public synchronized static native void lua_LunRef(long ptr, int t, int ref);

    public synchronized static native int lua_LgetN(long ptr, int t);

    public synchronized static native void lua_LsetN(long ptr, int t, int n);

    public synchronized static native int lua_LloadFile(long ptr, String fileName);

    public synchronized static native int lua_LloadBuffer(long ptr, byte[] buff, long sz, String name);

    public synchronized static native int lua_LloadString(long ptr, String s);

    public synchronized static native String lua_Lgsub(long ptr, String s, String p, String r);

    public synchronized static native String lua_LfindTable(long ptr, int idx, String fname, int szhint);


    public synchronized static native void lua_openBase(long ptr);

    public synchronized static native void lua_openTable(long ptr);

    public synchronized static native void lua_openIo(long ptr);

    public synchronized static native void lua_openOs(long ptr);

    public synchronized static native void lua_openString(long ptr);

    public synchronized static native void lua_openMath(long ptr);

    public synchronized static native void lua_openDebug(long ptr);

    public synchronized static native void lua_openPackage(long ptr);

    public synchronized static native void lua_openLibs(long ptr);

    /********************** Luajava API Library **********************/

    /**
     * Initializes lua State to be used by luajava
     *
     * @param ptr
     * @param stateId
     */
    public synchronized static native void fjlua_open(long ptr, int stateId);

    /**
     * register module begin
     *
     * @param ptr
     * @param name
     * @return
     */
    public synchronized static native boolean fjlua_beginModule(long ptr, String name);

    /**
     * register java class begin
     *
     * @param ptr
     * @param className
     * @return
     */
    public synchronized static native int fjlua_beginClass(long ptr, String className);

    /**
     * register java function
     *
     * @param ptr
     * @param funcName 方法名
     * @param funcId 方法id
     * @param isStatic 是否为静态方法
     * @return
     */
    public synchronized static native void fjlua_regFunc(long ptr, String funcName, int funcId, boolean isStatic);

    /**
     * register java class end
     *
     * @param ptr
     * @return
     */
    public synchronized static native void fjlua_endClass(long ptr);

    /**
     * register module end
     *
     * @param ptr
     */
    public synchronized static native void fjlua_endModule(long ptr);

    /**
     * push java obj
     *
     * @param ptr
     * @return
     */
    public synchronized static native void fjlua_pushJavaObj(long ptr, int metaRef, int objIndex);

    /**
     * Gets a Object from a userdata
     *
     * @param L
     * @param idx of the lua stack
     * @return Object
     */
    public synchronized static native int lua_getObjectFromUserdata(long L, int idx) throws LuaException;

    /**
     * Returns whether a userdata contains a Java Object
     *
     * @param L
     * @param idx index of the lua stack
     * @return boolean
     */
    public synchronized static native boolean lua_isJavaObject(long L, int idx);

}
