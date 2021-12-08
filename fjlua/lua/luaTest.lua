--元表  metatable

method = {
    __call = function(tab, ...)
        print(tab)
        for k, v in pairs({ ... }) do
            print("arg " .. k .. ' = ' .. tostring(v))
        end
        print('----method call ' .. tab.funcId)
    end
}

mt = {
    -- 常量定义位置
    c = 'ccc',

    -- 静态方法位置
    staticFunc = {
        type = 'method',
        funcId = 'staticFunc',
    },

    f1 = {
        type = 'field',
        get = 'getf1',
        set = 'setf1'
    },
    f2 = {
        type = 'field',
        get = 'getf2',
        set = 'gsetf2'
    },

    m1 = {
        type = 'method',
        funcId = 'method1',
    },
    m2 = {
        type = 'method',
        funcId = 'method2',
    },

    __index = function(tab, ...)
        print("__index " .. tostring(tab))
        local arg = {...}
        local name = arg[1]
        local mt =  getmetatable(tab);
        local info = mt[name]

        for k, v in pairs({ ... }) do
            print("arg " .. k .. ' = ' .. v)
        end
        if info.type == 'field' then
            return '----调用了__index ' .. name ..  ' func ' .. info['get']
        elseif info.type == 'method' then
            return info
        end
    end,

    __newindex = function(tab, ...)
        print("__newindex " .. tostring(tab))
        local arg = {...}
        local name = arg[1]
        local mt =  getmetatable(tab);
        local info = mt[name]

        for k, v in pairs({ ... }) do
            print("arg " .. k .. ' = ' .. v)
        end
        if info.type == 'field' then
            print( '----调用了__newindex ' .. name ..  ' func ' .. info['set'] .. ' = ' .. arg[2])
        elseif info.type == 'method' then
            return info
        end
    end
}
setmetatable(mt.staticFunc, method)
setmetatable(mt.m1, method)
setmetatable(mt.m2, method)

--metatable = setmetatable(metatable, t)

myTable = { 'Lua', 'Java', 'C#', 'C++' }
myTable = setmetatable(myTable, mt)--设置这个表的元表

print('getset: ' .. tostring(getset))
print('mt: ' .. tostring(mt))
print('m1: ' .. tostring(mt.m1))
print('m2: ' .. tostring(mt.m2))
print('myTable: ' .. tostring(myTable))

print()
print(myTable.f1)
myTable.f1 = 10

print()
print(mt.m1.funcId)
--print(mt.m1(777))
--print(mt:m1(777))
print(myTable.m1(777))
print(myTable:m1(777))

print(mt.c)
print(myTable.staticFunc())

print()
print(myTable.m1(777))
print(myTable.m2(7, 0, 8, 7))

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
    bubble_sort(arr)

    for i, v in ipairs(arr) do
        print(i, v)
    end
end

--sort()
