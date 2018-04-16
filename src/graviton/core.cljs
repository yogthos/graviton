(ns graviton.core
    (:require
     [clojure.walk :refer [prewalk postwalk]]
     [reagent.core :as r]))

;assets https://itch.io/game-assets/free/tag-2d

(defn load-texture [resource-name]
  (.fromImage (.-Texture js/PIXI) (str "assets/" resource-name)))

(defn sprite [resource-name]
  (js/PIXI.Sprite. (load-texture resource-name)))

(defn move-sprite [{:keys [sprite x y]}]
  (set! (.-x (.-position sprite)) x)
  (set! (.-y (.-position sprite)) y))

(defn add-to-stage [stage actor]
  (.addChild stage (:sprite actor)))

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
;; -------------------------
;; Views
(defn canvas [state]
  (r/create-class
   {:component-did-mount
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
        (render-loop state)))
    :render
    (fn [] [:canvas {:width 500 :height 500}])}))

(defn move-ship [ship width height]
  (-> ship
      (update :x #(if (> % width) -1 (inc %)))
      #_(update :y #(if (> % height) -1 (inc %)))))

(defn update-actors [actors width height]
  (postwalk
   (fn [node]
     (if (:sprite node)
       (let [updated-node ((:update node) node width height)]
         (move-sprite updated-node)
         updated-node)
       node))
   actors))

(defn update-game-state [{:keys [width height] :as state}]
  (update state :actors update-actors width height))

(defn game []
  (r/with-let [state (atom
                      {:update update-game-state
                       :actors [{:id :ship
                                 :sprite (sprite "ship.gif")
                                 :x 0
                                 :y 100
                                 :update move-ship}]})]
    [:div
     [:h2 "Graviton"]
     [canvas state]]))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [game] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
