(ns trajers.core
  (:gen-class)
  (:require [oz.core :as oz]
            [cheshire.core :as json]
            [incanter.stats :as stats]
            )
  (:import [ec.util MersenneTwister])
  )

;; Constants
(def agent-num 200)
(def agent-list (take agent-num (repeatedly rand)))
(def theta [0 1])
(def theta-weight 1)
(def epsilon [0 1])
(def epsilon-weight 1)

;; Draw functions
(defn draw-incant-norm
  [m sd]
  (stats/sample-normal 1 :mean m :sd sd)
  )
(defn draw
  [v]
  (apply draw-incant-norm v)
  )
(defn draw-u
  []
  (+ (* theta-weight (draw theta) (* epsilon-weight (draw epsilon))))
  )
(def init-mkt (rand))
(def model {:mkt init-mkt :al agent-list :pl []}) 

;(defn make-rng
;  [seed]
;  (let [rng (MersenneTwister. seed)]
;    (dotimes [_ 1500] (.nextInt rng))
;    rng
;    )
;  )
;(def my-rng (make-rng 42))

(defn update-agent-prices 
  [a pl] 
  (/ (+ a (apply + pl)) (+ 1  (count pl))))

(defn update-agents
  [m]
  (map #(update-agent-prices % (:pl m)) (:al m)))

(defn trade
  [a p]
  (- a p)
  )

(defn update-mkt
  [m]
  (+ (:mkt m) (/ (apply + (map #(trade % (:mkt m)) (:al m))) agent-num)))

(defn tick
  [m]
  (let [new-pl (conj (:pl m) (:mkt m))
        new-mkt (update-mkt m)
        new-agents (update-agents m)]
    {:mkt new-mkt :al new-agents :pl new-pl}))

;; Oz
(defn play-data [& names]
  (for [n names
        i (range 20)]
    {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))
(def line-plot
  {:data {:values (play-data "monkey" "slipper" "broom")}
   :encoding {:x {:field "time"}
              :y {:field "quantity"}
              :color {:field "item" :type "nominal"}}
   :mark "line"})

;(def rand-line-plot
;{:data {:values draws}
;:encoding
;{:x {:field "theta"}
;:y {:field "epsilon"}
;}
;mark "line"
;}
;)
(def stacked-bar
  {:data {:values (play-data "munchkin" "witch" "dog" "lion" "tiger" "bear")}
   :mark "bar"
   :encoding {:x {:field "time"
                  :type "ordinal"}
              :y {:aggregate "sum"
                  :field "quantity"
                  :type "quantitative"}
              :color {:field "item"
                      :type "nominal"}}})
(def contour-plot (oz/load "resources/contour-lines.vega.json"))
(def viz
  [:div
    [:h1 "Look ye and behold"]
    [:p "A couple of small charts"]
    [:div {:style {:display "flex" :flex-direction "row"}}
      [:vega-lite line-plot]
      [:vega-lite stacked-bar]]
    [:p "A wider, more expansive chart"]
    [:vega contour-plot]
    [:h2 "If ever, oh ever a viz there was, the vizard of oz is one because, because, because..."]
    [:p "Because of the wonderful things it does"]])


(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))
