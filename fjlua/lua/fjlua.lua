print 'load fjlua.lua'

function sum(count)
    --print "fjlua sum start"
    local a = 0;
    for i = 0, count do
        a = a + i
    end
    --print("fjlua sum end")
    return a;
end

Player = require("Game.Player")
--print()
--for k, v in pairs(Player) do
--    print(k, v)
--end
--print()
--for k, v in pairs(getmetatable(Player)) do
--    print(k, v)
--end
--print()
--print(Player.new_0)
--print(Player.new_0[1])
--print(Player.new_0[2])

function newInstance(count)
    --print "fjlua newInstance start"
    for i = 0, count do
        Player.new_0()
    end
    --print("fjlua newInstance end")
end

addExpPlayer = Player.new_0()
--print('addExpPlayer: ' .. addExpPlayer:getExp())

function playerAddExp(exp)
    --print "fjlua playerAddExp start"
    addExpPlayer:addExp(exp)
    --print "fjlua playerAddExp end"
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
    local arr = { 1, 20, -1, 30, 23, 21, -108, 55, 26, 55, -2, 2, 321, 324, 56, 34, -213, 43, -545, 67, -2, -4, 43, 2323, 54, 2356, 78, -32, 65, -721 }
    --print "fjlua sort start"
    bubble_sort(arr)
    --print "fjlua sort end"
end

function fib(n)
    if n == 0 then
        return 0
    elseif n == 1 then
        return 1
    end
    return fib(n-1) + fib(n-2)
end
