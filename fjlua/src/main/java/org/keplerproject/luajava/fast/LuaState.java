/*
 * $Id: LuaState.java,v 1.11 2007-09-17 19:28:40 thiago Exp $
 * Copyright (C) 2003-2007 Kepler Project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.keplerproject.luajava.fast;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * LuaState if the main class of LuaJava for the Java developer.
 * LuaState is a mapping of most of Lua's C API functions.
 * LuaState also provides many other functions that will be used to manipulate
 * objects between Lua and Java.
 *
 * @author Thiago Ponte
 */
@SuppressWarnings("unused")
public class LuaState {
    /**
     * lua虚拟机在c中的指针地址
     */
    private long luaStatePtr;
    /**
     * jvm中lua虚拟机索引
     */
    private final int stateIndex;

    /**
     * lua中可使用的类，
     */
    private final Map<Class<?>, ForLuaClass<?>> metaMap = new ConcurrentHashMap<>();
    /**
     * lua中可用方法
     */
    private final ArrayList<FunctionInfo<?>> funcs = new ArrayList<>(128);

    /**
     * lua中可使用的类，
     */
    private final Int2ObjectMap<ForLuaClass<?>> metaRefMap = new Int2ObjectArrayMap<>();
    /**
     * 对象唯一id，传入到lua中的对象
     * 在这里保持强引用，等待lua gc回收再同步这里回收
     */
    private final Cache<Integer, Object> objCache = Caffeine.newBuilder().build();
    /**
     * 对象唯一id生成器
     */
    private final AtomicInteger objIndexGenerate = new AtomicInteger();

    /**
     * Constructor to instance a new LuaState and initialize it with LuaJava's functions
     *
     * @param stateIndex
     */
    protected LuaState(int stateIndex) {
        luaStatePtr = FjluaDll.lua_open();
        FjluaDll.fjlua_open(luaStatePtr, stateIndex);
        FjluaDll.lua_openPackage(luaStatePtr);
        this.stateIndex = stateIndex;
    }

    /**
     * Receives a existing state and initializes it
     *
     * @param luaStatePtr
     */
    protected LuaState(long luaStatePtr) {
        this.luaStatePtr = luaStatePtr;
        this.stateIndex = LuaStateFactory.insertLuaState(this);
        FjluaDll.fjlua_open(luaStatePtr, stateIndex);
    }

    /**
     * Closes state and removes the object from the LuaStateFactory
     */
    public void close() {
        LuaStateFactory.removeLuaState(stateIndex);
        FjluaDll.lua_close(luaStatePtr);
        this.luaStatePtr = 0;
    }

    /**
     * Returns <code>true</code> if state is closed.
     */
    public boolean isClosed() {
        return luaStatePtr == 0;
    }

    /**
     * 注册java class meta
     */
    public <T> void registerClass(Class<T> clz, Consumer<ForLuaClass.Builder<T>> filler) {
        metaMap.computeIfAbsent(clz, aClass -> {
            int ref = FjluaDll.fjlua_beginClass(luaStatePtr, aClass.getSimpleName());

            ForLuaClass.Builder<T> builder = new ForLuaClass.Builder<>(clz, ref);
            filler.accept(builder);

            ForLuaClass<T> luaClass = builder.build();

            //注册方法
            for (FunctionInfo<T> func : luaClass.getFunctions()) {
                int funcId = funcs.size();
                funcs.add(func);
                func.setFuncId(funcId);
                FjluaDll.fjlua_regFunc(luaStatePtr, func.getFunctionName(), funcId, func.isStaticFunc());
            }
            //注册字段


            FjluaDll.fjlua_endClass(luaStatePtr);

            metaRefMap.put(luaClass.getRef(), luaClass);
            return luaClass;
        });
    }

    /**
     * 关联java对象到lua虚拟机
     *
     * @param obj
     */
    public void pushJavaObject(Object obj) {
        int objIndex = objIndexGenerate.incrementAndGet();
        objCache.put(objIndex, obj);
        ForLuaClass<?> forLuaClass = metaMap.get(obj.getClass());
        FjluaDll.fjlua_pushJavaObj(luaStatePtr, forLuaClass.getRef(), objIndex);
    }

    /**
     * 获取保持的对象
     */
    public @Nullable Object getForLuaObject(long objIndex) {
        return objCache.getIfPresent(objIndex);
    }

    /**
     * 获取保持的对象，如果未查找到对象，抛出异常
     */
    public @NotNull Object getForLuaObjOrThrowException(int objIndex) throws LuaException {
        Object obj = objCache.getIfPresent(objIndex);
        if (obj == null) {
            throw new LuaException("not found object from obj cache with index " + objIndex);
        }
        return obj;
    }

    /**
     * 从lua栈中获取java对象
     */
    public Object getObjectFromUserdata(int idx) throws LuaException {
        int objIndex = FjluaDll.lua_getObjectFromUserdata(luaStatePtr, idx);
        return getForLuaObject(objIndex);
    }

    /**
     * 释放对象
     */
    void releaseObj(int objIndex) {
        objCache.invalidate(objIndex);
    }

    /**
     * 被lua持有的缓存中java对象数量
     *
     * @return
     */
    public int forLuaObjSize() {
        return (int) objCache.estimatedSize();
    }

    public ForLuaClass<?> getForLuaClass(int metaRef) {
        return metaRefMap.get(metaRef);
    }

    public FunctionInfo<?> getForLuaFunction(int funcId) {
        return funcs.get(funcId);
    }

    public ForLuaClass<?> getForLuaClass(Class<?> clz) {
        return metaMap.get(clz);
    }

    public long getLuaStatePtr() {
        return luaStatePtr;
    }

    public int getStateIndex() {
        return stateIndex;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LuaState luaState = (LuaState) o;

        return stateIndex == luaState.stateIndex;
    }

    @Override
    public int hashCode() {
        return stateIndex;
    }

    /**
     * Tells whether a lua index contains a java Object
     *
     * @param idx index of the lua stack
     * @return boolean
     */
    public boolean isJavaObject(int idx) {
        return FjluaDll.lua_isJavaObject(luaStatePtr, idx);
    }

    /**
     * Pushes into the stack any object value.<br>
     * This function checks if the object could be pushed as a lua type, if not
     * pushes the java object.
     *
     * @param obj
     */
    public void pushObjectValue(Object obj) throws LuaException {
        if (obj == null) {
            pushNil();
        } else if (obj instanceof Boolean) {
            Boolean bool = (Boolean) obj;
            pushBoolean(bool);
        } else if (obj instanceof Number) {
            pushNumber(((Number) obj).doubleValue());
        } else if (obj instanceof String) {
            pushString((String) obj);
        } else if (obj instanceof LuaObject) {
            LuaObject ref = (LuaObject) obj;
            ref.push();
        } else if (obj instanceof byte[]) {
            pushString((byte[]) obj);
        } else {
            pushJavaObject(obj);
        }
    }

    /**
     * Function that returns a Java Object equivalent to the one in the given
     * position of the Lua Stack.
     *
     * @param idx Index in the Lua Stack
     * @return Java object equivalent to the Lua one
     */
    public synchronized Object toJavaObject(int idx) throws LuaException {
        Object obj = null;

        if (isBoolean(idx)) {
            obj = toBoolean(idx);
        } else if (type(idx) == FjluaDll.LUA_TSTRING) {
            obj = toString(idx);
        } else if (isFunction(idx)) {
            obj = getLuaObject(idx);
        } else if (isTable(idx)) {
            obj = getLuaObject(idx);
        } else if (type(idx) == FjluaDll.LUA_TNUMBER) {
            obj = toNumber(idx);
        } else if (isUserdata(idx)) {
            if (isJavaObject(idx)) {
                obj = getObjectFromUserdata(idx);
            } else {
                obj = getLuaObject(idx);
            }
        }

        return obj;
    }

    /**
     * Creates a reference to an object in the variable globalName
     *
     * @param globalName
     * @return LuaObject
     */
    public LuaObject getLuaObject(String globalName) {
        return new LuaObject(this, globalName);
    }

    /**
     * Creates a reference to an object inside another object
     *
     * @param parent The Lua Table or Userdata that contains the Field.
     * @param name   The name that index the field
     * @return LuaObject
     * @throws LuaException if parent is not a table or userdata
     */
    public LuaObject getLuaObject(LuaObject parent, String name)
        throws LuaException {
        if (parent.L.getLuaStatePtr() != luaStatePtr) {
            throw new LuaException("Object must have the same LuaState as the parent!");
        }

        return new LuaObject(parent, name);
    }

    /**
     * This constructor creates a LuaObject from a table that is indexed by a number.
     *
     * @param parent The Lua Table or Userdata that contains the Field.
     * @param name   The name (number) that index the field
     * @return LuaObject
     * @throws LuaException When the parent object isn't a Table or Userdata
     */
    public LuaObject getLuaObject(LuaObject parent, Number name)
        throws LuaException {
        if (parent.L.getLuaStatePtr() != luaStatePtr) {
            throw new LuaException("Object must have the same LuaState as the parent!");
        }

        return new LuaObject(parent, name);
    }

    /**
     * This constructor creates a LuaObject from a table that is indexed by any LuaObject.
     *
     * @param parent The Lua Table or Userdata that contains the Field.
     * @param name   The name (LuaObject) that index the field
     * @return LuaObject
     * @throws LuaException When the parent object isn't a Table or Userdata
     */
    public LuaObject getLuaObject(LuaObject parent, LuaObject name)
        throws LuaException {
        if (parent.getLuaState().getLuaStatePtr() != luaStatePtr ||
            parent.getLuaState().getLuaStatePtr() != name.getLuaState().getLuaStatePtr()) {
            throw new LuaException("Object must have the same LuaState as the parent!");
        }

        return new LuaObject(parent, name);
    }

    /**
     * Creates a reference to an object in the <code>index</code> position
     * of the stack
     *
     * @param index position on the stack
     * @return LuaObject
     */
    public LuaObject getLuaObject(int index) {
        return new LuaObject(this, index);
    }

    /**
     * When you call a function in lua, it may return a number, and the
     * number will be interpreted as a <code>Double</code>.<br>
     * This function converts the number into a type specified by
     * <code>retType</code>
     *
     * @param db      lua number to be converted
     * @param retType type to convert to
     * @return The converted number
     */
    public static Number convertLuaNumber(Double db, Class<?> retType) {
        // checks if retType is a primitive type
        if (retType.isPrimitive()) {
            if (retType == Integer.TYPE) {
                return db.intValue();
            } else if (retType == Long.TYPE) {
                return db.longValue();
            } else if (retType == Float.TYPE) {
                return db.floatValue();
            } else if (retType == Double.TYPE) {
                return db;
            } else if (retType == Byte.TYPE) {
                return db.byteValue();
            } else if (retType == Short.TYPE) {
                return db.shortValue();
            }
        } else if (retType.isAssignableFrom(Number.class)) {
            // Checks all possibilities of number types
            if (retType.isAssignableFrom(Integer.class)) {
                return db.intValue();
            } else if (retType.isAssignableFrom(Long.class)) {
                return db.longValue();
            } else if (retType.isAssignableFrom(Float.class)) {
                return db.floatValue();
            } else if (retType.isAssignableFrom(Double.class)) {
                return db;
            } else if (retType.isAssignableFrom(Byte.class)) {
                return db.byteValue();
            } else if (retType.isAssignableFrom(Short.class)) {
                return db.shortValue();
            }
        }

        // if all checks fail, return null
        return null;
    }

    public LuaState newThread() {
        LuaState l = new LuaState(FjluaDll.lua_newthread(luaStatePtr));
        LuaStateFactory.insertLuaState(l);
        return l;
    }

    // STACK MANIPULATION

    public int getTop() {
        return FjluaDll.lua_getTop(luaStatePtr);
    }

    public void setTop(int idx) {
        FjluaDll.lua_setTop(luaStatePtr, idx);
    }

    public void pushValue(int idx) {
        FjluaDll.lua_pushValue(luaStatePtr, idx);
    }

    public void remove(int idx) {
        FjluaDll.lua_remove(luaStatePtr, idx);
    }

    public void insert(int idx) {
        FjluaDll.lua_insert(luaStatePtr, idx);
    }

    public void replace(int idx) {
        FjluaDll.lua_replace(luaStatePtr, idx);
    }

    public int checkStack(int sz) {
        return FjluaDll.lua_checkStack(luaStatePtr, sz);
    }

    public void xmove(LuaState to, int n) {
        FjluaDll.lua_xmove(luaStatePtr, to.luaStatePtr, n);
    }

    // ACCESS FUNCTION

    public boolean isNumber(int idx) {
        return (FjluaDll.lua_isNumber(luaStatePtr, idx) != 0);
    }

    public boolean isString(int idx) {
        return (FjluaDll.lua_isString(luaStatePtr, idx) != 0);
    }

    public boolean isFunction(int idx) {
        return (FjluaDll.lua_isFunction(luaStatePtr, idx) != 0);
    }

    public boolean isCFunction(int idx) {
        return (FjluaDll.lua_isCFunction(luaStatePtr, idx) != 0);
    }

    public boolean isUserdata(int idx) {
        return (FjluaDll.lua_isUserdata(luaStatePtr, idx) != 0);
    }

    public boolean isTable(int idx) {
        return (FjluaDll.lua_isTable(luaStatePtr, idx) != 0);
    }

    public boolean isBoolean(int idx) {
        return (FjluaDll.lua_isBoolean(luaStatePtr, idx) != 0);
    }

    public boolean isNil(int idx) {
        return (FjluaDll.lua_isNil(luaStatePtr, idx) != 0);
    }

    public boolean isThread(int idx) {
        return (FjluaDll.lua_isThread(luaStatePtr, idx) != 0);
    }

    public boolean isNone(int idx) {
        return (FjluaDll.lua_isNone(luaStatePtr, idx) != 0);
    }

    public boolean isNoneOrNil(int idx) {
        return (FjluaDll.lua_isNoneOrNil(luaStatePtr, idx) != 0);
    }

    public int type(int idx) {
        return FjluaDll.lua_type(luaStatePtr, idx);
    }

    public String typeName(int tp) {
        return FjluaDll.lua_typeName(luaStatePtr, tp);
    }

    public int equal(int idx1, int idx2) {
        return FjluaDll.lua_equal(luaStatePtr, idx1, idx2);
    }

    public int rawequal(int idx1, int idx2) {
        return FjluaDll.lua_rawequal(luaStatePtr, idx1, idx2);
    }

    public int lessthan(int idx1, int idx2) {
        return FjluaDll.lua_lessthan(luaStatePtr, idx1, idx2);
    }

    public double toNumber(int idx) {
        return FjluaDll.lua_toNumber(luaStatePtr, idx);
    }

    public int toInteger(int idx) {
        return FjluaDll.lua_toInteger(luaStatePtr, idx);
    }

    public boolean toBoolean(int idx) {
        return (FjluaDll.lua_toBoolean(luaStatePtr, idx) != 0);
    }

    public String toString(int idx) {
        return FjluaDll.lua_toString(luaStatePtr, idx);
    }

    public int strLen(int idx) {
        return FjluaDll.lua_strlen(luaStatePtr, idx);
    }

    public int objLen(int idx) {
        return FjluaDll.lua_objlen(luaStatePtr, idx);
    }

    public LuaState toThread(int idx) {
        return new LuaState(FjluaDll.lua_toThread(luaStatePtr, idx));
    }

    //PUSH FUNCTIONS

    public void pushNil() {
        FjluaDll.lua_pushNil(luaStatePtr);
    }

    public void pushNumber(double db) {
        FjluaDll.lua_pushNumber(luaStatePtr, db);
    }

    public void pushInteger(int integer) {
        FjluaDll.lua_pushInteger(luaStatePtr, integer);
    }

    public void pushString(String str) {
        if (str == null) {
            FjluaDll.lua_pushNil(luaStatePtr);
        } else {
            FjluaDll.lua_pushString(luaStatePtr, str);
        }
    }

    public void pushString(byte[] bytes) {
        if (bytes == null) {
            FjluaDll.lua_pushNil(luaStatePtr);
        } else {
            FjluaDll.lua_pushString(luaStatePtr, bytes, bytes.length);
        }
    }

    public void pushBoolean(boolean bool) {
        FjluaDll.lua_pushBoolean(luaStatePtr, bool ? 1 : 0);
    }

    // GET FUNCTIONS

    public void getTable(int idx) {
        FjluaDll.lua_getTable(luaStatePtr, idx);
    }

    public void getField(int idx, String k) {
        FjluaDll.lua_getField(luaStatePtr, idx, k);
    }

    public void rawGet(int idx) {
        FjluaDll.lua_rawGet(luaStatePtr, idx);
    }

    public void rawGetI(int idx, int n) {
        FjluaDll.lua_rawGetI(luaStatePtr, idx, n);
    }

    public void createTable(int narr, int nrec) {
        FjluaDll.lua_createTable(luaStatePtr, narr, nrec);
    }

    public void newTable() {
        FjluaDll.lua_newTable(luaStatePtr);
    }

    // if returns 0, there is no metatable
    public int getMetaTable(int idx) {
        return FjluaDll.lua_getMetaTable(luaStatePtr, idx);
    }

    public void getFEnv(int idx) {
        FjluaDll.lua_getFEnv(luaStatePtr, idx);
    }

    // SET FUNCTIONS

    public void setTable(int idx) {
        FjluaDll.lua_setTable(luaStatePtr, idx);
    }

    public void setField(int idx, String k) {
        FjluaDll.lua_setField(luaStatePtr, idx, k);
    }

    public void rawSet(int idx) {
        FjluaDll.lua_rawSet(luaStatePtr, idx);
    }

    public void rawSetI(int idx, int n) {
        FjluaDll.lua_rawSetI(luaStatePtr, idx, n);
    }

    // if returns 0, cannot set the metatable to the given object
    public int setMetaTable(int idx) {
        return FjluaDll.lua_setMetaTable(luaStatePtr, idx);
    }

    // if object is not a function returns 0
    public int setFEnv(int idx) {
        return FjluaDll.lua_setFEnv(luaStatePtr, idx);
    }

    public void call(int nArgs, int nResults) {
        FjluaDll.lua_call(luaStatePtr, nArgs, nResults);
    }

    // returns 0 if ok of one of the error codes defined
    public int pcall(int nArgs, int nResults, int errFunc) {
        return FjluaDll.lua_pcall(luaStatePtr, nArgs, nResults, errFunc);
    }

    public int yield(int nResults) {
        return FjluaDll.lua_yield(luaStatePtr, nResults);
    }

    public int resume(int nArgs) {
        return FjluaDll.lua_resume(luaStatePtr, nArgs);
    }

    public int status() {
        return FjluaDll.lua_status(luaStatePtr);
    }

    public int gc(int what, int data) {
        return FjluaDll.lua_gc(luaStatePtr, what, data);
    }

    public int getGcCount() {
        return FjluaDll.lua_getGcCount(luaStatePtr);
    }

    public int next(int idx) {
        return FjluaDll.lua_next(luaStatePtr, idx);
    }

    public int error() {
        return FjluaDll.lua_error(luaStatePtr);
    }

    public void concat(int n) {
        FjluaDll.lua_concat(luaStatePtr, n);
    }


    // FUNCTION FROM lauxlib
    // returns 0 if ok
    public int LdoFile(String fileName) {
        return FjluaDll.lua_LdoFile(luaStatePtr, fileName);
    }

    // returns 0 if ok
    public int LdoString(String str) {
        return FjluaDll.lua_LdoString(luaStatePtr, str);
    }

    public int LgetMetaField(int obj, String e) {
        return FjluaDll.lua_LgetMetaField(luaStatePtr, obj, e);
    }

    public int LcallMeta(int obj, String e) {
        return FjluaDll.lua_LcallMeta(luaStatePtr, obj, e);
    }

    public int Ltyperror(int nArg, String tName) {
        return FjluaDll.lua_Ltyperror(luaStatePtr, nArg, tName);
    }

    public int LargError(int numArg, String extraMsg) {
        return FjluaDll.lua_LargError(luaStatePtr, numArg, extraMsg);
    }

    public String LcheckString(int numArg) {
        return FjluaDll.lua_LcheckString(luaStatePtr, numArg);
    }

    public String LoptString(int numArg, String def) {
        return FjluaDll.lua_LoptString(luaStatePtr, numArg, def);
    }

    public double LcheckNumber(int numArg) {
        return FjluaDll.lua_LcheckNumber(luaStatePtr, numArg);
    }

    public double LoptNumber(int numArg, double def) {
        return FjluaDll.lua_LoptNumber(luaStatePtr, numArg, def);
    }

    public int LcheckInteger(int numArg) {
        return FjluaDll.lua_LcheckInteger(luaStatePtr, numArg);
    }

    public int LoptInteger(int numArg, int def) {
        return FjluaDll.lua_LoptInteger(luaStatePtr, numArg, def);
    }

    public void LcheckStack(int sz, String msg) {
        FjluaDll.lua_LcheckStack(luaStatePtr, sz, msg);
    }

    public void LcheckType(int nArg, int t) {
        FjluaDll.lua_LcheckType(luaStatePtr, nArg, t);
    }

    public void LcheckAny(int nArg) {
        FjluaDll.lua_LcheckAny(luaStatePtr, nArg);
    }

    public int LnewMetatable(String tName) {
        return FjluaDll.lua_LnewMetatable(luaStatePtr, tName);
    }

    public void LgetMetatable(String tName) {
        FjluaDll.lua_LgetMetatable(luaStatePtr, tName);
    }

    public void Lwhere(int lvl) {
        FjluaDll.lua_Lwhere(luaStatePtr, lvl);
    }

    public int Lref(int t) {
        return FjluaDll.lua_Lref(luaStatePtr, t);
    }

    public void LunRef(int t, int ref) {
        FjluaDll.lua_LunRef(luaStatePtr, t, ref);
    }

    public int LgetN(int t) {
        return FjluaDll.lua_LgetN(luaStatePtr, t);
    }

    public void LsetN(int t, int n) {
        FjluaDll.lua_LsetN(luaStatePtr, t, n);
    }

    public int LloadFile(String fileName) {
        return FjluaDll.lua_LloadFile(luaStatePtr, fileName);
    }

    public int LloadString(String s) {
        return FjluaDll.lua_LloadString(luaStatePtr, s);
    }

    public int LloadBuffer(byte[] buff, String name) {
        return FjluaDll.lua_LloadBuffer(luaStatePtr, buff, buff.length, name);
    }

    public String Lgsub(String s, String p, String r) {
        return FjluaDll.lua_Lgsub(luaStatePtr, s, p, r);
    }

    public String LfindTable(int idx, String fname, int szhint) {
        return FjluaDll.lua_LfindTable(luaStatePtr, idx, fname, szhint);
    }

    //IMPLEMENTED C MACROS

    public void pop(int n) {
        FjluaDll.lua_pop(luaStatePtr, n);
    }

    public synchronized void getGlobal(String global) {
        FjluaDll.lua_getGlobal(luaStatePtr, global);
    }

    public synchronized void setGlobal(String name) {
        FjluaDll.lua_setGlobal(luaStatePtr, name);
    }

    // Functions to open lua libraries
    public void openBase() {
        FjluaDll.lua_openBase(luaStatePtr);
    }

    public void openTable() {
        FjluaDll.lua_openTable(luaStatePtr);
    }

    public void openIo() {
        FjluaDll.lua_openIo(luaStatePtr);
    }

    public void openOs() {
        FjluaDll.lua_openOs(luaStatePtr);
    }

    public void openString() {
        FjluaDll.lua_openString(luaStatePtr);
    }

    public void openMath() {
        FjluaDll.lua_openMath(luaStatePtr);
    }

    public void openDebug() {
        FjluaDll.lua_openDebug(luaStatePtr);
    }

    public void openPackage() {
        FjluaDll.lua_openPackage(luaStatePtr);
    }

    public void openLibs() {
        FjluaDll.lua_openLibs(luaStatePtr);
    }


}
