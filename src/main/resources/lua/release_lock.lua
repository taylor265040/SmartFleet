-- release_lock.lua
-- Redis Lua 释放锁脚本（原子性：校验持有者 + 删除）
-- KEYS[1] = sf:lock:vehicle:{vehicleId}
-- ARGV[1] = userId
-- 返回：1=释放成功，0=不是持有者或锁不存在

local key = KEYS[1]
local userId = ARGV[1]

-- 检查当前持有者
local currentOwner = redis.call('GET', key)
if not currentOwner then
    -- 锁不存在
    return 0
end

if currentOwner == userId then
    -- 是持有者，删除锁
    redis.call('DEL', key)
    return 1
else
    -- 不是持有者
    return 0
end
