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

(defn set-sprite-position [{:keys [sprite x y velocity width height]}]
  (set! (.-x (.-position sprite)) x)
  (set! (.-y (.-position sprite)) y)
  (set! (.-rotation sprite) (js/Math.atan2 (:y velocity) (:x velocity)))
  (when width (set! (.-width sprite) width))
  (when height (set! (.-height sprite) height)))

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

(defn canvas-coords [canvas]
  [(int (.-width canvas))
   (int (.-height canvas))])

(defn init-renderer [canvas width height]
  (js/PIXI.autoDetectRenderer width height (clj->js {:view canvas})))

(defn render [renderer stage]
  (.render renderer stage))

(defn gravitational-acceleration-at-point [px py actors]
  (apply merge-with +
         (map (fn [{:keys [x y mass]}] (let [dx    (- px x)
                                             dy    (- py y)
                                             r     (/ mass (+ (* dx dx) (* dy dy) 0.0000001))
                                             theta (js/Math.atan (/ dy dx))]
                                         {:x (* (if (> px x) -1 1) r (js/Math.cos theta))
                                          :y (* (if (>= px x) -1 1) r (js/Math.sin theta))})) actors)))

(defn sigmoid [v]
  (/ v (+ 1 (js/Math.abs v))))

(defn draw-line [graphics {:keys [color width start end]}]
  (doto graphics
    (.lineStyle width color)
    (.moveTo (:x start) (:y start))
    (.lineTo (:x end) (:y end))))

(defn draw-gravity-vector [graphics x y state]
  (let [{ax :x ay :y :as acceleration} (gravitational-acceleration-at-point x y (filterv #(not= (:id %) :ship) (:actors state)))
        ax         (* 50 ax)
        ay         (* 50 ay)
        magnitude  (+ (* ax ax) (* ay ay))
        redness    1
        greenness  70
        max-length 4
        color      (+ (* (js/Math.round (* 0xff (sigmoid (* magnitude redness)))) 0x10000) (- 0xff00 (* (js/Math.round (* 0xff (sigmoid (/ magnitude greenness)))) 0x100)))
        width      (* 0.75 (sigmoid (* 5 magnitude)))]
    (draw-line graphics {:color color :width width {:x x :y y} {:x (+ x (* max-length (sigmoid ax))) :y (+ y (* max-length (sigmoid ay)))}})))

(defn draw-vector-field [state & [spacing]]
  (let [spacing (or spacing 5)]
    (.clear (:vector-field state))
    (doall (for [x (map #(* spacing %) (range (js/Math.ceil (/ (:width state) spacing))))
                 y (map #(* spacing %) (range (js/Math.ceil (/ (:height state) spacing))))]
             (draw-gravity-vector (:vector-field state) x y state)))))

(defn render-loop [state-atom]
  ((fn frame []
     (let [{:keys [renderer stage actors vector-field] :as state} @state-atom]
       (render renderer stage)
       (js/requestAnimationFrame frame)))))

(defn add-actors-to-stage [state]
  (let [{:keys [stage actors]} @state]
    (prewalk
      (fn [node] (when (:sprite node) (add-to-stage stage node)) node)
      actors)))

(defn init-game-loop [state]
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
    (let [canvas       (r/dom-node component)
          width        (int (.-width canvas))
          height       (int (.-height canvas))
          vector-field (js/PIXI.Graphics.)
          stage        (init-stage)
          ticker       (js/PIXI.ticker.Ticker.)]
      (swap! state assoc
             :vector-field vector-field
             :canvas canvas
             :width width
             :height height
             :stage stage
             :renderer (init-renderer canvas width height)
             :ticker ticker)
      (add-stage-on-click-event state)
      (draw-vector-field @state)
      (.addChild stage vector-field)
      (add-actors-to-stage state)
      (init-game-loop state)
      (.start ticker)
      (render-loop state))))
