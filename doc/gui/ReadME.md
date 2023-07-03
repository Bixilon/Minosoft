# GUI/HUD

Minosoft integrates a custom gui system that can be used to display pretty much anything in 2d space on the screen.

## Stages

GUI has 3 rendering stages:

- update mode (you can make changes async, prepare the next data, `tick`, ...)
- layout mode (called eventually every frame, all currently stored data is applied and put in the layout)
- rendering mode (the current layout will be rendered, might be called every frame if you do animations)
