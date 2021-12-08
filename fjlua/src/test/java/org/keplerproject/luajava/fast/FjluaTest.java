package org.keplerproject.luajava.fast;

import org.openjdk.jmh.annotations.Benchmark;

/**
 * @author wl
 * @version 2021.11.15
 */
public class FjluaTest implements ILuaTest {

    private static final LuaState luaState;

    static {
        System.loadLibrary("lib/lua51");
//        System.loadLibrary("lib/fjlua-1.0-SNAPSHOT");
        System.load("D:\\repository\\fjlua_dll\\x64\\Debug\\fjlua_dll.dll");

        luaState = LuaStateFactory.newLuaState();
        luaState.openLibs();

        FjluaDll.fjlua_beginModule(luaState.getLuaStatePtr(), null);
        FjluaDll.fjlua_beginModule(luaState.getLuaStatePtr(), "Game");

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
        FjluaDll.fjlua_endModule(luaState.getLuaStatePtr());
        FjluaDll.fjlua_endModule(luaState.getLuaStatePtr());

        luaState.LdoFile("lua/fjlua.lua");
    }

    public static void main(String[] args) {
        FjluaTest test = new FjluaTest();
        System.out.println(test.sum());
//        test.newInstance();
        test.methodCall();
        test.sort();
    }

    @Benchmark
    @Override
    public long sum() {
        luaState.getGlobal("sum");
        luaState.pushInteger(count);
        if (luaState.pcall(1, 1, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
        int sum = luaState.toInteger(-1);
        luaState.pop(1);
        return sum;
    }

    @Benchmark
    @Override
    public void newInstance() {
//        System.out.println(luaState.forLuaObjSize());

        luaState.getGlobal("newInstance");
        luaState.pushInteger(count);
        if (luaState.pcall(1, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
        luaState.gc(FjluaDll.LUA_GCCOLLECT, 0);

//        System.out.println(luaState.forLuaObjSize());
    }

    @Benchmark
    @Override
    public void methodCall() {
        luaState.getGlobal("playerAddExp");
        luaState.pushInteger(count);
        if (luaState.pcall(1, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
    }

    @Benchmark
    @Override
    public void sort() {
        luaState.getGlobal("sort");
        if (luaState.pcall(0, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
    }

    @Benchmark
    @Override
    public long fib() {
        luaState.getGlobal("fib");
        luaState.pushInteger(fibNum);
        if (luaState.pcall(1, 0, 0) != 0) {
            System.out.println("lua error: " + luaState.toString(-1));
            System.out.println("lua error: " + luaState.toString(-2));
        }
        long num = (long) luaState.toNumber(-1);
        luaState.pop(1);
        return num;
    }
}
