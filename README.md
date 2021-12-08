# fjlua
参考c# [tolua](https://github.com/topameng/tolua )框架的实现逻辑，优化[luajava](https://github.com/jasonsantos/luajava)中lua调用java代码的性能  

**重写java对象在lua中的管理方式**   
**重写java对象方法的调用方式**  
**使用了luajit**  

目前的版本属于不完善版本，未经过完整测试  
待完善：java对象池监控体系，java注册方法到lua中很繁琐需要开发一个自动生成注册工具  

简单性能测试数据 `fjlua` `luaj` `luajava`  
| Benchmark                |  Mode | Cnt |  Score  |  Units |
| -------------------------|-------|-----|---------|--------|
| FjluaTest.methodCall     | thrpt |  2  |1392.487 |  ops/ms|
| FjluaTest.newInstance    | thrpt |  2  |   0.012 |  ops/ms|
| FjluaTest.sort           | thrpt |  2  | 569.080 |  ops/ms|
| FjluaTest.sum            | thrpt |  2  |  13.973 |  ops/ms|
| LuajTest.methodCall      | thrpt |  2  |9476.718 |  ops/ms|
| LuajTest.newInstance     | thrpt |  2  |   0.084 |  ops/ms|
| LuajTest.sort            | thrpt |  2  |  23.356 |  ops/ms|
| LuajTest.sum             | thrpt |  2  |   0.380 |  ops/ms|
| LuajavaTest.methodCall   | thrpt |  2  | 180.450 |  ops/ms|
| LuajavaTest.newInstance  | thrpt |  2  |   0.005 |  ops/ms|
| LuajavaTest.sort         | thrpt |  2  | 507.555 |  ops/ms|
| LuajavaTest.sum          | thrpt |  2  |  13.937 |  ops/ms|
| FjluaTest.methodCall     |  avgt |  2  |   0.001 |   ms/op|
| FjluaTest.newInstance    |  avgt |  2  |  79.379 |   ms/op|
| FjluaTest.sort           |  avgt |  2  |   0.002 |   ms/op|
| FjluaTest.sum            |  avgt |  2  |   0.071 |   ms/op|
| LuajTest.methodCall      |  avgt |  2  |  ≈ 10⁻⁴  |   ms/op|
| LuajTest.newInstance     |  avgt |  2  |  11.505 |   ms/op|
| LuajTest.sort            |  avgt |  2  |   0.043 |   ms/op|
| LuajTest.sum             |  avgt |  2  |   2.641 |   ms/op|
| LuajavaTest.methodCall   |  avgt |  2  |   0.006 |   ms/op|
| LuajavaTest.newInstance  |  avgt |  2  | 208.608 |   ms/op|
| LuajavaTest.sort         |  avgt |  2  |   0.002 |   ms/op|
| LuajavaTest.sum          |  avgt |  2  |   0.072 |   ms/op|

## 主要优化点  

### 删除 class `CPtr`，改为直接使用 `long` 来存档 `luavm` 地址
> `CPtr` 用于存放 `luavm` 的内存地址，在c代码中获取 `luavm` 每次都需要使用 `jni` 从 `CPtr` 实例中获取到luavm
#### C  
```C
lua_State * getStateFromCPtr( JNIEnv * env , jobject cptr )
{
   lua_State * L;

   jclass classPtr       = ( *env )->GetObjectClass( env , cptr );
   jfieldID CPtr_peer_ID = ( *env )->GetFieldID( env , classPtr , "peer" , "J" );
   jbyte * peer          = ( jbyte * ) ( *env )->GetLongField( env , cptr , CPtr_peer_ID );

   L = ( lua_State * ) peer;

   pushJNIEnv( env ,  L );

   return L;
}
```
> 修改后减少一次jni调用
#### C  
```C
static lua_State* getStateFromCPtr(JNIEnv* env, jlong cptr)
{
    return (lua_State*)cptr;
}
```

### 修改java对象与lua的交互方式
> luajava提前注册了 `LuajavaApi` 类作为lua调用java方法的媒介，该类中大量使用了反射进行处理  

> 修改方式参考 `tolua` 的实现  
1. 在java中维护对象池
   #### java  
```java
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
```
2. 在lua中提前注册需要传递对象的 class/field/method 为lua metatable, 并关联 `__index` `__newIndex`为自定义的 `luaCFunction`，关联__gc用于释放java对象
#### C  
```C
JNIEXPORT jint JNICALL Java_org_keplerproject_luajava_fast_FjluaDll_fjlua_1beginClass
(JNIEnv* env, jobject jobj, jlong cptr, jstring className) {
    lua_State* L = getStateFromCPtr(env, cptr);
    //fprintf(stderr, "beginClass 1 top: %d \n", lua_gettop(L));
    const char* str = (*env)->GetStringUTFChars(env, className, NULL);
    //压类名 入栈 1
    lua_pushstring(L, str);
    (*env)->ReleaseStringUTFChars(env, className, str);

    //新建table1 入栈 2
    lua_newtable(L);
    //放入table loaded
    _addtoloaded(L);

    lua_newtable(L);
    lua_pushvalue(L, -1);
    int reference = luaL_ref(L, LUA_REGISTRYINDEX);
    //fprintf(stderr, "beginClass 1 top: %d, reference = %d \n", lua_gettop(L), reference);
    lua_pushlightuserdata(L, &javaObjTag);
    lua_pushnumber(L, 1);
    lua_rawset(L, -3);

    //table1[.name] = className
    lua_pushstring(L, ".name");
    _pushfullname(L, -4);
    lua_rawset(L, -3);

    lua_pushstring(L, "__index");
    lua_pushvalue(L, -2);
    lua_rawset(L, -3);

    lua_pushstring(L, "__gc");
    lua_pushcfunction(L, &javaObjGc);
    lua_rawset(L, -3);

    //fprintf(stderr, "beginClass 2 top: %d \n", lua_gettop(L));
    return reference;
}
```
#### java  
```java
 luaState.registerClass(Player.class, playerBuilder -> {
                playerBuilder.addFunction("addExp", (L, player) -> {
                    int exp = L.toInteger(-1);
                    player.addExp(exp);
                    return 0;
                });
                playerBuilder.addFunction("getExp", (L, player) -> {
                    L.pushNumber(player.getExp());
                    return 1;
                });
                playerBuilder.addFunction("setExp", (L, player) -> {
                    int exp = L.toInteger(-1);
                    player.setExp(exp);
                    return 0;
                });
                playerBuilder.addStaticFunction("new_0", L -> {
                    L.pushJavaObject(new Player());
                    return 1;
                });
            }
        );
```
3. 传递到lua中时使用对象索引 `int`放入lua_userdata，关联已注册的 metatable
#### C
```C
JNIEXPORT void JNICALL Java_org_keplerproject_luajava_fast_FjluaDll_fjlua_1pushJavaObj
(JNIEnv* env, jobject jobj, jlong cptr, jint metaRef, jint index) {
    lua_State* L = getStateFromCPtr(env, cptr);

    //fprintf(stderr, "pushJavaObj 1 top: %d \n", lua_gettop(L));
    // 获取c#对象index管理table
    lua_getref(L, LUA_RIDX_UBOX);

    //fprintf(stderr, "pushJavaObj 2 top: %d \n", lua_gettop(L));
    //创建一块新的内存区域，存放index
    fjlua_newUserData(L, index);
    //fprintf(stderr, "pushJavaObj 3 top: %d \n", lua_gettop(L));
    //获取index对应c#数据提前注册创建号的meta数据
    lua_getref(L, metaRef);
    //关联meta数据
    lua_setmetatable(L, -2);

    //并不是往栈顶插入元素-1， 而是把在栈中位置为-1的元素copy之后插入于栈顶中
    lua_pushvalue(L, -1);

    //栈索引-3 处的table[index] = 上面复制的对象数据（userdata）
    // tableIndex 就是 c#对象index管理table
    lua_rawseti(L, -3, index);

    //fprintf(stderr, "pushJavaObj 5 top: %d \n", lua_gettop(L));
    // 从给定有效索引处移除一个元素， 把这个索引之上的所有元素移下来填补上这个空隙。
    lua_remove(L, -2);
}
```
4. lua调用时借用lua元表元方法的机制调用到已注册的 `luaCFunction`，再jni调用传递 `funcId` `objIndex` 到java端，实现调用
```java
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
```
