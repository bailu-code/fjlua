package org.keplerproject.luajava.fast;

public interface ILuaTest {
    int count = 100000;
    int fibNum = 35;

    /**
     * 求和 0-10w
     * @return
     */
    long sum();

    /**
     * 生成 10w 对象
     */
    void newInstance();

    /**
     * 调用 Player#addExp 10w 对象
     */
    void methodCall();

    /**
     * 调用sort
     */
    void sort();

    /**
     * 斐波拉契数列
     */
    long fib();

}
