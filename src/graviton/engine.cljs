(ns graviton.engine
  (:require
    [clojure.walk :refer [prewalk]]
    [reagent.core :as r]))

;http://pixijs.io/examples/#/basics/basic.js

(defn load-texture [resource-name]
  (.fromImage (.-Texture js/PIXI) (str "assets/" resource-name)))

(defn sprite [resource-name]
  (let [sprite (js/PIXI.Sprite. (load-texture resource-name))]
    (.set (.-anchor sprite) 0.5 0.5)
    sprite))

(defn set-sprite-position [{:keys [sprite x y velocity width height] :as entity}]
  (set! (.-x (.-position sprite)) x)
  (set! (.-y (.-position sprite)) y)
  (set! (.-rotation sprite) (js/Math.atan2 (:y velocity) (:x velocity)))
  (when width (set! (.-width sprite) width))
  (when height (set! (.-height sprite) height))
  entity)

(defn add-to-stage [stage actor]
  (set-sprite-position actor)
  (.addChild stage (:sprite actor)))

(defn remove-actors-from-stage [state]
  (let [{:keys [stage actors]} state]
    (doseq [actor actors]
      (.removeChild stage (:sprite actor)))))

(defn remove-from-stage [stage actor]
  (.removeChild stage (:sprite actor)))

(defn init-stage []
  (js/PIXI.Container.))

(defn init-renderer [canvas width height]
  (js/PIXI.autoDetectRenderer width height (clj->js {:view canvas})))

(defn render [renderer stage]
  (.render renderer stage))

(defn draw-line [graphics {:keys [color width start end]}]
  (doto graphics
    (.lineStyle width color)
    (.moveTo (:x start) (:y start))
    (.lineTo (:x end) (:y end))))

(defn draw-circle [graphics {:keys [color x y radius style]}]
  (doto graphics
    (.lineStyle (or style 0))
    (.beginFill color)
    (.drawCircle x y radius)
    (.endFill)))

(defn render-loop [state-atom]
  ((fn frame []
     (let [{:keys [renderer stage] :as state} @state-atom]
       (render renderer stage)
       (js/requestAnimationFrame frame)))))

(defn add-actors-to-stage [state]
  (let [{:keys [stage actors]} @state]
    (prewalk
      (fn [node] (when (:sprite node) (add-to-stage stage node)) node)
      actors)))

(defn init-game-loop [state]
  (doseq [{:keys [init] :as object} (:objects @state)]
    (init object @state))
  (.add (:ticker @state)
        (fn [delta]
          (swap! state
                 #((:update %) (assoc % :delta delta))))))

(defn add-drag-start-event [object handler]
  (if handler
    (doto object
      (.on "mousedown" handler)
      (.on "touchstart" handler))
    object))

(defn add-drag-event [object handler]
  (if handler
    (doto object
      (.on "mousemove" handler)
      (.on "touchmove" handler))
    object))

(defn add-drag-end-event [object handler]
  (if handler
    (doto object
      (.on "mouseup" handler)
      (.on "mouseupoutside" handler)
      (.on "touchend" handler)
      (.on "touchendoutside" handler))
    object))

(defn drag-event [object state {:keys [on-start on-move on-end]}]
  (-> object
      (add-drag-start-event (when on-start (partial on-start state)))
      (add-drag-event (when on-move (partial on-move state)))
      (add-drag-end-event (when on-end (partial on-end state)))))

(defn click-coords [stage event]
  (let [point (.getLocalPosition (.-data event) stage)]
    {:x (.-x point) :y (.-y point)}))

(defn add-stage-on-click-event [state]
  (let [{:keys [stage on-click on-drag width height]} @state]
    (let [background-layer (js/PIXI.Container.)
          hit-area         (js/PIXI.Rectangle. 0 0 width height)]
      (set! (.-interactive background-layer) true)
      (set! (.-buttonMode background-layer) true)
      (set! (.-hitArea background-layer) hit-area)
      (.addChild stage background-layer)
      (when on-drag
        (drag-event background-layer state on-drag))
      (when on-click
        (set! (.-click background-layer) (partial on-click state))))))

(defn init-canvas [state]
  (fn [component]
    (let [canvas (r/dom-node component)
          width  (int (.-width canvas))
          height (int (.-height canvas))
          stage  (init-stage)
          ticker (js/PIXI.ticker.Ticker.)]
      (swap! state assoc
             :canvas canvas
             :width width
             :height height
             :stage stage
             :renderer (init-renderer canvas width height)
             :ticker ticker)
      (add-stage-on-click-event state)
      (add-actors-to-stage state)
      (init-game-loop state)
      (.start ticker)
      (render-loop state))))
