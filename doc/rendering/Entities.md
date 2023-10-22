# Entity rendering

Entity rendering is a quite hard topic.

## Classes

- Player renderer
    - player model
    - feature renderer:
        - armor
        - stuck arrows

## Skeletal models

- SkeletalModel (raw data)
- BakedSkeletalModel (baked mesh)
- SkeletalInstance (renders baked mesh, contains transform values)
- Wrapper (indirectly accesses skeletal instance transforms)

## Model designing

### Entities

Entities are always designed without any rotation (i.e. `yaw`=`0`)

## Things to consider

- name rendering (without culling and visible through walls) (and scoreboard objective)
- hitbox rendering
- entity model itself
    - player with arms, legs, ...
    - has animations, ...
    - different poses (sneaking, etc)
- yaw vs head yaw
- "features"
    - armor (and armor trims)
    - elytra
    - stuck arrows (and bee stingers)
    - cape
    - shoulder entities (parrots)
    - held item
- light (shade and lightmap)
- 
