## CC: Tweaked peripheral information

The controller hub and the tweaked controller hub have the same peripheral methods.

---

```lua
listChannels() -> string[]
```

Returns a list of all the channels the connected hub has available. 
For example, the non-tweaked hub has the channels:
```lua
["keyUp", "keyDown", "keyLeft", ...]
```

But the tweaked hub has the channels:
```lua
["axisLeftX+", "axisLeftX-", "axisLeftY+", ...]
```

---
```lua
getChannelValue(channel: string) -> int
```

Returns the redstone strength of the channel given (in the range `0..15`).

Throws if:
- The computer is not on a ship (DBW only works on ships)
- The channel name doesn't exist on the connected hub

---
```lua
setChannelValue(channel: string, value: int)
```

Sets the redstone strength of the channel, essentially pretending to be a connected controller.

Throws if:
- `value` is not in the range `0..15`
- The channel name doesn't exist on the connected hub
- The channel is not connected to any wires

---

When connected to a hub peripheral, there is also a new event that can be queried.

The `controller_changed` event will trigger any time a linked controller changes its output (including when it changes back to 0).

Example usage:
```lua
-- The peripheral must be connected for the event to trigger,
-- even if you don't use the peripheral.
local hub = peripheral.wrap("top")

while true do
    local event, channel, value = os.pullEvent("controller_changed")
    
    -- Ignore when it changes back to 0 because that will fill up our logs
    if value ~= 0 then
        print(channel)
        print(value)
    end
end
```