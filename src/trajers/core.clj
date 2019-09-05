(ns trajers.core
  (:gen-class)
  (:require [oz.core :as oz]
            [cheshire.core :as json]
            [incanter.stats :as stats]
            )
  )

;; Constants
(def agent-num 200)
(def theta [0 1])
(def theta-weight 1)
(def epsilon [0 1])
(def epsilon-weight 1)

(def norm-mean 0)
(def norm-std 1)
(def inform-frac 0.5)

;; Utilities
(defn round2
  "Round a double to the given precision (number of significant digits)"
  [precision d]
  (let [factor (Math/pow 10 precision)]
    (/ (Math/round (* d factor)) factor)))
(defn uuid
  []
  (keyword (str (java.util.UUID/randomUUID))))

;; Draw functions
(defn draw-incant-norm
  ([]
   (stats/sample-normal 1 :mean norm-mean :sd norm-std))
  ([n]
   (stats/sample-normal n :mean norm-mean :sd norm-std))
  ([m sd]
   (stats/sample-normal 1 :mean m :sd sd)))
(defn draw-round
  []
  (round2 2 (draw-incant-norm)))
(defn draw
  [v]
  (apply draw-incant-norm v)
  )
(defn draw-u
  []
  (+ (* theta-weight (draw theta) (* epsilon-weight (draw epsilon))))
  )

;; Agent functions
(defn prior-vals
  [a]
  (mapv :value (:prior a)))
(defn move-away?
  [old-price new-price prior]
  (let [abs-val (fn [p] (Math/abs (- prior p)))]
    (> (abs-val new-price) (abs-val old-price)))
  )
;; TODO This isn't actually what I want! I don't want them to move that one prior closer to price, I want them to move that prior closer to price MINUS the rest of the vector. So this is a bit more complicated. Probably explains weirdness I am getting in graphs.
(defn avg-update
  [p prior]
  (/ (+ (:value prior) p) 2))
(defn rand-uninf
  [v]
  (let [r (rand-int (count v))]
    (cond
      (and (= (count v) 1) (:informed? (get v r))) nil
      (:informed? (get v r)) (recur v)
      :else r)
    )
  )
(defn prior-update
  [pri pred upd]
  (let [i (rand-uninf pri)
        sel-pri (get pri i)]
    (cond
      (and sel-pri pred) (update pri i #(assoc % :value (upd sel-pri)))
      :else pri 
      )))
(defn make-agent
  ([n]
   {:prior (vec (repeatedly n (fn [] {:informed? false :value (draw-round)}))) 
    :id (uuid)})
  ([n prior]
   (let [valv (vec (repeatedly n (fn [] {:informed? false :value (draw-round)})))
         i (rand-int n)]
     {:prior (assoc valv i {:informed? true :value (get prior i)}) 
      :id (uuid)}))
  )
;; Market functions
(defn make-agent-list
  [n inf-prior]
  (into []
        (concat (repeatedly (* agent-num inform-frac) (partial make-agent n inf-prior))
                (repeatedly (* (- 1 inform-frac) agent-num) (partial make-agent n))))
  )
(defn make-market
  [n]
  (let [security (vec (repeatedly n draw-round))
        start-price (draw-round)]
    {:security security
     :price start-price
     :sp start-price
     :agents (make-agent-list n security)}
    ))
(defn- update-price
  [p pe]
  (+ p (cond
                  (> pe 0) 0.01
                  (< pe 0) -0.01
                  :else 0))
  ;(+ (* pe 0.01) (:price m))
  )
(defn- take-orders
  [al p]
  (let [take-order (fn [a]
                     (let [prior (reduce + (prior-vals a))
                           id (:id a)]
                       (cond
                         (> p prior) {id :sl}
                         (< p prior) {id :bl}
                         :else nil)))
        orders (filter #(not (nil? %)) (mapv #(take-order %) al))
        [sl bl e] (let [[sl bl] (mapv #(set (keys (into {} %)))
                               ((juxt filter remove) #(= :sl (val (first %))) orders))
                        blcount (count bl)
                        slcount (count sl)
                        e (- blcount slcount)]
                    (conj (cond
                             (> slcount blcount) [(set (take blcount sl)) bl]
                             (> blcount slcount) [sl (set (take slcount bl))]
                             :else [sl bl])
                          e))
        add-stock (fn [a s]
                    (cond
                      (vector? (:holdings a)) (assoc a :holdings (conj (:holdings a) s))
                      :else (assoc a :holdings [s])))
         ]
    [(mapv (fn [a] (cond
                      (contains? sl (:id a)) (add-stock a p)
                      (contains? bl (:id a)) (add-stock a (- p))
                      :else a)) al) e]
    )
  )

(defn market-update
  [m]
  (let [price (:price m)
        sp (:sp m)
        up-agents (mapv (fn [a] (let [prior (:prior a)]
                                  (-> a
                                      (assoc :prior
                                             (prior-update
                                              prior
                                              (move-away? sp price (reduce + (prior-vals a)))
                                              (partial avg-update price)))))) (:agents m))
        [new-agents pe] (take-orders up-agents price)]
    (-> m
        (assoc :sp price)
        (assoc :agents new-agents)
        (assoc :price (update-price price pe))
        ))
  )

;; Oz
(defn make-data
  [ms]
  (vec (mapcat #(let [priors (mapv (fn [a] (:value (first (:prior a)))) (:agents %2))
                      infs (mapv (fn [a] (:informed? (first (:prior a)))) (:agents %2))]
                  (mapv (fn [prior inf] (hash-map :time %1
                                                  :price (:price %2)
                                                  :prior prior
                                                  :informed? inf)) priors infs))
               (range (count ms)) ms))
  )
(defn make-snap-data
  [ms]
  (vec (mapcat #(let [priors (mapv (fn [a] (prior-vals a)) (:agents %2))
                      infs (mapv (fn [a] (mapv :informed? (:prior a))) (:agents %2))]
                  (mapv (fn [prior inf] (hash-map :time %1
                                                  :price1 (first (:security %2)) 
                                                  :price2 (second (:security %2)) 
                                                  :prior1 (first prior)
                                                  :prior2 (second prior)
                                                  :informed? (if (first inf) 1 2)
                                                  )) priors infs))
               (range (count ms)) ms)))
(def m (make-market 2))
(def mt (make-data (take 1000 (iterate market-update m))))
(def snap (make-snap-data (take 2000 (iterate market-update m))))
(def price-plot
  {:height 600
   :width 800
   :data {:values mt}
   :layer [{:encoding {:x {:field "time"}
                       :y {:field "prior"}
                       :color {:field "informed?"}}
            :mark {:type "point" :opacity 0.3}}
           {:encoding {:x {:field "time"}
                       :y {:field "price"}}
            :mark {:type "line"}}]
   })
(def mst (filter #(= (:time %) 1999) snap))
(def price-snap-plot
  {:height 600
   :width 800
   :data {:values mst}
   :layer [{:encoding {:x {:field "prior1"}
                       :y {:field "prior2"}
                       :color {:field "informed?"}}
            :mark {:type "point" :opacity 0.3}}
           {:encoding {:x {:field "price1"}
                       :y {:field "price2"}}
            :mark {:type "point" :shape "square" :size 60}}]
   })
(oz/view! price-snap-plot)
(defn plot-snap
  [d n]
  (oz/view! {:height 600
   :width 800
   :data {:values (filter #(= (:time %) n) d)}
   :layer [{:encoding {:x {:field "prior1"}
                       :y {:field "prior2"}
                       :color {:field "informed?"}}
            :mark {:type "point" :opacity 0.3}}
           {:encoding {:x {:field "price1"}
                       :y {:field "price2"}}
            :mark {:type "point" :shape "square" :size 60}}]
   }))
;(oz/start-server!)

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))


;; Scratch
;; (def init-mkt (rand))
;; (def model {:mkt init-mkt :al agent-list :pl []}) 

;; ;(defn make-rng
;; ;  [seed]
;; ;  (let [rng (MersenneTwister. seed)]
;; ;    (dotimes [_ 1500] (.nextInt rng))
;; ;    rng
;; ;    )
;; ;  )
;; ;(def my-rng (make-rng 42))

;; (defn update-agent-prices 
;;   [a pl] 
;;   (/ (+ a (apply + pl)) (+ 1  (count pl))))

;; (defn update-agents
;;   [m]
;;   (map #(update-agent-prices % (:pl m)) (:al m)))

;; (defn trade
;;   [a p]
;;   (- a p)
;;   )

;; (defn update-mkt
;;   [m]
;;   (+ (:mkt m) (/ (apply + (map #(trade % (:mkt m)) (:al m))) agent-num)))

;; (defn tick
;;   [m]
;;   (let [new-pl (conj (:pl m) (:mkt m))
;;         new-mkt (update-mkt m)
;;         new-agents (update-agents m)]
;;     {:mkt new-mkt :al new-agents :pl new-pl}))

;; (defn play-data [& names]
;;   (for [n names
;;         i (range 20)]
;;     {:time i :item n :quantity (+ (Math/pow (* i (count n)) 0.8) (rand-int (count n)))}))
;; (def line-plot
;;   {:data {:values (play-data "monkey" "slipper" "broom")}
;;    :encoding {:x {:field "time"}
;;               :y {:field "quantity"}
;;               :color {:field "item" :type "nominal"}}
;;    :mark "line"})

;(def rand-line-plot
;{:data {:values draws}
;:encoding
;{:x {:field "theta"}
;:y {:field "epsilon"}
;}
;mark "line"
;}
;)
;; (def stacked-bar
;;   {:data {:values (play-data "munchkin" "witch" "dog" "lion" "tiger" "bear")}
;;    :mark "bar"
;;    :encoding {:x {:field "time"
;;                   :type "ordinal"}
;;               :y {:aggregate "sum"
;;                   :field "quantity"
;;                   :type "quantitative"}
;;               :color {:field "item"
;;                       :type "nominal"}}})
;; (def contour-plot (oz/load "resources/contour-lines.vega.json"))
;; (def viz
;;   [:div
;;     [:h1 "Look ye and behold"]
;;     [:p "A couple of small charts"]
;;     [:div {:style {:display "flex" :flex-direction "row"}}
;;       [:vega-lite line-plot]
;;       [:vega-lite stacked-bar]]
;;     [:p "A wider, more expansive chart"]
;;     [:vega contour-plot]
;;     [:h2 "If ever, oh ever a viz there was, the vizard of oz is one because, because, because..."]
;;     [:p "Because of the wonderful things it does"]])
