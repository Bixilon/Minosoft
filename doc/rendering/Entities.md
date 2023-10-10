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
