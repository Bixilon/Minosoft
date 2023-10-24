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

## General

- render layers (opaque -> transparent -> translucent -> ...) (but face culling enabled)
    - sort in layers after distance (or -distance)
    - there are also invisible renderers (like AreaEffectCloud is just emitting particles)
- update all models async (with their visibility, etc)
- queue for unloading and loading meshes before draw (better while async preparing to save time. Maybe port that system to block entities)
- Loop over all visible entity renderers and work on the entity layer as needed
- update visible and not visible
    - entity name is also visible through walls, rest not
    - also with frustum (no need to update renderers that are out of the frustum)
    - -> handle occlusion differently from visibility
- option to turn on/off "features"
- how to register entity models?
    - loop over all entity (and block entity) types and register?
- store entities in octree like structure
    - ways faster collisions with them -> physics
    - way faster getInRadius -> particles, maybe entities in the future

## Hitboxes

- Create line mesh with default aabb
    - interpolate
        - aabb (not at all, taken from renderInfo)
        - color: 0.5s
        - velocity (ticks)
        - direction (taken from renderInfo)
        - eye height (taken from renderInfo)
    - they must alight with the matrix handler -> mustn't be a frame too late/early
- Store offset and rotation as uniform
- Make hitbox a default feature of entity renderer
- highest priority (enables cheap gpu clipping)
- dynamic enabling and disabling (hitbox manager, keybinding)

## Tests

- hitbox (data, loading, unloading)
- collect visible meshes (with sorting, priority type and distance)
