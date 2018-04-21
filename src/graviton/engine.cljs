(ns graviton.engine
  (:require [reagent.core :as r]))

(defn load-texture [resource-name]
  (.fromImage (.-Texture js/PIXI) (str "assets/" resource-name)))

(defn sprite [resource-name]
  (js/PIXI.Sprite. (load-texture resource-name)))

(defn move-sprite [{:keys [sprite x y]}]
  (set! (.-x (.-position sprite)) x)
  (set! (.-y (.-position sprite)) y))

(defn add-to-stage [stage actor]
  (.addChild stage (:sprite actor)))

(defn remove-from-stage [stage actor]
  (.removeChild stage (:sprite actor)))

(defn init-stage []
  (js/PIXI.Container.))

(defn canvas-coords [canvas]
  [(int (.-width canvas))
   (int (.-height canvas))])

(defn init-renderer [canvas width height]
  (js/PIXI.autoDetectRenderer width height (clj->js {:view canvas})))

(defn render [renderer stage]
  (.render renderer stage))

(defn render-loop [state]
  ((fn frame []
     (let [{:keys [renderer stage actors]} @state]
       (render renderer stage)
       (js/requestAnimationFrame frame)))))

(defn add-actors-to-stage [state]
  (let [{:keys [stage actors]} @state]
    (prewalk
      (fn [node] (when (:sprite node) (add-to-stage stage node)) node)
      actors)))

(defn game-loop [state]
  (add-actors-to-stage state)
  ((fn tick []
     (let [{:keys [update]} @state]
       (js/setTimeout tick 10 (swap! state update))))))

(defn init-canvas [state]
  (fn [component]
    (let [canvas (r/dom-node component)
          width  (int (.-width canvas))
          height (int (.-height canvas))]
      (swap! state assoc
             :canvas canvas
             :width width
             :height height
             :stage (init-stage)
             :renderer (init-renderer canvas width height))
      (game-loop state)
      (render-loop state))))
