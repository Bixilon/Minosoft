# Bannability

This document covers how likely you can get banned.

**Note: This may not cover everything, I am NOT responsible for anti cheat banned accounts, use this client at your own
risk**

Generally minosoft tries to mimic minecraft vanilla, but not everything is done the same way, either due to my or
minecraft's design.

## Interactions

(when breaking a block, attacking an entity, ...)

### NoSwing

You might get flagged for no swing (not swinging your arm when attacking), because minecraft swings its arm way too
often.
When breaking a block a `SwingArm` packet is sent twice in vanilla and then every tick. Those ticks are aligned to all
other ticks.
Minosoft though tries to avoid all tick aligning for async reasons. You will just send one swing arm packet instantly
and then every 50ms after it another.

## Target handling

Target gets checked every frame (so it is smooth and one tick behind).
That is also a big difference. Imho this is not cheating at all, but - depending on the anti cheat - you will get
flagged for using killaura.

## Hypixel

Big question, short answer: I don't know. I played a couple of rounds and it worked, I was not banned yet.
But don't take my word for it.
