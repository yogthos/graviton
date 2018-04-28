(ns graviton.engine
  (:require
    [clojure.walk :refer [prewalk]]
    [reagent.core :as r]))

;http://pixijs.io/examples/#/basics/basic.js

(defn load-texture [resource-name]
  (.fromImage (.-Texture js/PIXI) (str "assets/" resource-name)))

(defn set-anchor [obj x y]
  (.set (.-anchor obj) x y)
  obj)

(defn sprite [resource-name]
  (let [sprite (js/PIXI.Sprite. (load-texture resource-name))]
    (set-anchor sprite 0.5 0.5)))

(defn set-graphics-position [{:keys [graphics x y velocity width height] :as entity}]
  (set! (.-x (.-position graphics)) x)
  (set! (.-y (.-position graphics)) y)
  (when velocity (set! (.-rotation graphics) (js/Math.atan2 (:y velocity) (:x velocity))))
  (when width (set! (.-width graphics) width))
  (when height (set! (.-height graphics) height))
  entity)

(defn sort-by-z-index [stage]
  (-> (.-children stage)
      (.sort (fn [a b] (< (.-zOrder b) (.-zOrder a))))))

(defn add-actor-to-stage [{:keys [stage] :as state}
                          {:keys [init] :as actor}]
  (let [{:keys [graphics z-index] :as actor} (init actor state)]
    (set! (.-zOrder graphics) (or z-index 0))
    (set-graphics-position actor)
    (.addChild stage graphics)
    (sort-by-z-index stage)
    actor))

(defn remove-from-stage [stage actor]
  (.removeChild stage (:graphics actor)))

(defn clear-stage [{:keys [background actors foreground stage]}]
  (doseq [object (concat background actors foreground)]
    (remove-from-stage stage object)))

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

(defn draw-circle [graphics {:keys [line-color fill-color x y radius line-thickness opacity]}]
  (when line-color
    (.lineStyle graphics (or line-thickness 3) line-color))
  (when fill-color
    (.beginFill graphics fill-color (or opacity 1))
    (.drawCircle graphics x y radius (or opacity 1)))
  (.endFill graphics))

(defn add-actors-to-stage [state]
  (vswap! state update :actors #(map (partial add-actor-to-stage @state) %)))

(defn add-objects-to-stage [state type]
  (vswap! state update type #(mapv (fn [{:keys [init] :as object}]
                                     (init object @state))
                                   %)))

(defn add-background-to-stage [state]
  (add-objects-to-stage state :background))

(defn add-foreground-to-stage [state]
  (add-objects-to-stage state :foreground))

(defn started? [{:keys [game-state]}]
  (= :started game-state))

(defn render-loop [state-atom]
  ((fn frame []
     (let [{:keys [renderer stage]} @state-atom]
       (render renderer stage)
       (js/requestAnimationFrame frame)))))

(defn init-render-loop [state]
  (.add (:ticker @state)
        (fn [delta]
          (when (started? @state)
            (vswap! state #((:update %) (assoc % :delta delta)))))))

(defn add-drag-start-event [object handler]
  (if handler
    (doto object
      (.on "pointerdown" handler))
    object))

(defn add-drag-event [object handler]
  (if handler
    (doto object
      (.on "pointermove" handler))
    object))

(defn add-drag-end-event [object handler]
  (if handler
    (doto object
      (.on "pointerup" handler)
      (.on "pointerupoutside" handler))
    object))

(defn drag-event [object state {:keys [on-start on-move on-end]}]
  (-> object
      (add-drag-start-event (when on-start (partial on-start @state)))
      (add-drag-event (when on-move (partial on-move @state)))
      (add-drag-end-event (when on-end (fn [event] (vswap! state #(or (on-end % event) %)))))))

(defn click-coords [stage event]
  (let [point (.getLocalPosition (.-data event) stage)]
    {:x (.-x point) :y (.-y point)}))

(defn init-scene [state]
  (add-background-to-stage state)
  (add-actors-to-stage state)
  (add-foreground-to-stage state))

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

(defn init-canvas [state init-fn]
  (fn [component]
    (let [canvas (r/dom-node component)
          width  (int (.-width canvas))
          height (int (.-height canvas))
          stage  (init-stage)
          ticker (js/PIXI.ticker.Ticker.)]
      (vswap! state assoc
              :canvas canvas
              :width width
              :height height
              :stage stage
              :renderer (init-renderer canvas width height)
              :ticker ticker)
      (vswap! state init-fn)
      (add-stage-on-click-event state)
      (init-scene state)
      (init-render-loop state)
      (.start ticker)
      (render-loop state))))
