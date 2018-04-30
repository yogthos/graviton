## Graviton

Graviton is a physics based game where the player collects green prizes while avoiding red death zones.

The player adds gravitational attractors on the screen by clicking and dragging the mouse,
and the ship follows the gravity fields. The more gravitational attractors are added the more
death zones will be spawned on the screen.

[play the game in the browser](https://svmbrown.itch.io/graviton)

### Development mode

To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.
Once Figwheel starts up, you should be able to open the `public/index.html` page in the browser.


### Building for production

```
lein clean
lein package
```
