-- preemptive_lock.lua
-- Redis Lua 预占锁脚本（原子性：校验 + 设置 + 过期）
-- KEYS[1] = sf:lock:vehicle:{vehicleId}
-- ARGV[1] = userId, ARGV[2] = lockSeconds
-- 返回：1=成功获取锁，0=已被其他人锁定，-1=已是持有者（续期）

local key = KEYS[1]
local userId = ARGV[1]
local lockSeconds = tonumber(ARGV[2])

-- 检查是否已被锁
local currentOwner = redis.call('GET', key)
if currentOwner == userId then
    -- 已是持有者，续期
    redis.call('EXPIRE', key, lockSeconds)
    return -1
end
if currentOwner then
    -- 被其他人锁定
    return 0
end
-- 未锁定，获取锁
redis.call('SETEX', key, lockSeconds, userId)
return 1
