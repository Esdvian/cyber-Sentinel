local key = KEYS[1]
local now = tonumber(ARGV[1])
local replenishRate = tonumber(ARGV[2])
local burstCapacity = tonumber(ARGV[3])

local tokenKey = key .. ':tokens'
local timestampKey = key .. ':timestamp'

local lastTokens = tonumber(redis.call('GET', tokenKey))
if lastTokens == nil then
    lastTokens = burstCapacity
end

local lastTimestamp = tonumber(redis.call('GET', timestampKey))
if lastTimestamp == nil then
    lastTimestamp = now
end

local delta = math.max(0, now - lastTimestamp) / 1000.0
local filledTokens = math.min(burstCapacity, lastTokens + (delta * replenishRate))

if filledTokens < 1 then
    redis.call('SET', tokenKey, filledTokens)
    redis.call('SET', timestampKey, now)
    redis.call('PEXPIRE', tokenKey, 60000)
    redis.call('PEXPIRE', timestampKey, 60000)
    return 0
end

filledTokens = filledTokens - 1
redis.call('SET', tokenKey, filledTokens)
redis.call('SET', timestampKey, now)
redis.call('PEXPIRE', tokenKey, 60000)
redis.call('PEXPIRE', timestampKey, 60000)
return 1
