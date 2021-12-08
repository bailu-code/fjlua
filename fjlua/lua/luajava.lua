function sum(count)
    --print "luajava sum start"
    local a = 0;
    for i = 0, count do
        a = a + i
    end
    --print "luajava sum end"
    return a;
end

--require("luajava")
playerClass = luajava.bindClass("org.keplerproject.luajava.fast.Player")

function newInstance(count)
    --print "luajava newInstance start"
    for i = 0, count do
        luajava.new(playerClass)
    end
    --print "luajava newInstance end"
end

addExpPlayer = luajava.new(playerClass)
function playerAddExp(exp)
    --print "luajava playerAddExp start"
    addExpPlayer:addExp(exp)
    --print "luajava playerAddExp end"
end

function bubble_sort(arr)
    for i = 1, #arr - 1 do
        for j = 1, #arr - i do
            if arr[j] > arr[j + 1] then
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
            end
        end
    end
end

function sort()
    --print "luajava sort start"
    local arr = { 1, 20, -1, 30, 23, 21, -108, 55, 26, 55, -2, 2, 321, 324, 56, 34, -213, 43, -545, 67, -2, -4, 43, 2323, 54, 2356, 78, -32, 65, -721 }
    bubble_sort(arr)
    --print "luajava sort end"
end

function fib(n)
    if n == 0 then
        return 0
    elseif n == 1 then
        return 1
    end
    return fib(n-1) + fib(n-2)
end
