# Physics

The physics should not have anything todo with rendering and also should not use inheritance, that is really complicated and makes the entity classes huge. Instead, a pipeline (similar to the network pipeline from netty) will be used. Every entity has its own pipeline instance and every part of it is its own instance. The pipeline can be modified per entity, because (client sided) most entities have an empty pipeline, some of them do velocity handling and the local player has the biggest pipeline. The pipeline should not use high level entity implementation, use the lowest abstraction as possible. A part of a pipeline is identified by a string

Example parts of the pipeline:

- Movement packet sending
- Velocity handling
- …
