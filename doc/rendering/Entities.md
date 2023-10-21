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

### Block entities

Block entity models (e.g. chests) are always `facing`=`north` by default.

### Entities

Entities are always designed without any rotation (i.e. `yaw`=`0`)
