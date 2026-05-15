# jme-animation-fsm
For more details visit the [Wiki](https://github.com/capdevon/jme-capdevon-examples/wiki) section.

🔔 If you found this project useful, please let me know by leaving a star to motivate me to improve it. Thanks.

## YouTube videos
- [AnimatorStateMachine: BlendTree 1D](https://youtu.be/rVFFjLQMysQ)
- [AnimatorStateMachine: BlendTree 2D](https://www.youtube.com/watch?v=G-Cd120hSlI)
- [AnimatorStateMachine: StateMachineBehaviour](https://youtu.be/AQkUT5U48co)
- [AnimatorStateMachine: Multilayer & AvatarMask](https://youtu.be/jBGju49DScI)

## Requirements
- [jmonkeyengine](https://github.com/jMonkeyEngine/jmonkeyengine) - A complete 3D game development suite written purely in Java.
- [Minie](https://github.com/stephengold/Minie) - A physics library for JMonkeyEngine.
- jdk8+
    
## Assets
- [Mixamo](https://www.mixamo.com/)
- [Blender](https://www.blender.org/download/)

## Bug report / feature request
The best way to report bug or feature request is [github's issues page](https://github.com/capdevon/jme-animation-fsm/issues).

## Why this project?

`jme-animation-fsm` extends the standard jMonkeyEngine animation workflow with a lightweight and modular animation state machine system inspired by modern game engine animators.

### Highlights

- **Finite State Machine based animation flow**  
  Organize character logic into states and transitions instead of manual animation switching.

- **Blend Trees**  
  Smoothly blend animations using parameters such as speed or direction.

- **Animation Layers & Avatar Masks**  
  Combine upper-body and lower-body animations independently (for example: running while aiming).

- **Hierarchical State Machines**  
  Build complex animation logic using nested state machines while keeping transitions manageable.

- **State Behaviours**  
  Attach gameplay-related callbacks to animation states (`onEnter`, `onUpdate`, `onExit`).

- **Built on top of jMonkeyEngine animation system**  
  Integrates with `AnimComposer` and existing jME animation workflows instead of replacing them.

- **Code-driven and extensible**  
  Designed for developers who prefer full control through Java code without requiring a visual editor.

### Project Scope

This project is intended as an experimental and extensible foundation for advanced character animation systems in jMonkeyEngine.

It is not a full replacement for AAA animation graph tools such as Unity Animator or Unreal Animation Blueprints, but it aims to provide many of the same core concepts in a lightweight open-source form.
