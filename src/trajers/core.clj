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
(defn make-agent
  ([sp]
   {:prior (draw-round) :informed? false :sp sp
    :id (uuid)})
  ([sp prior]
   {:prior prior :informed? true :sp sp
    :id (uuid)})
  )
(defn agent-learn
  [a p]
  (let [{prior :prior
         sp :sp} a 
        abs-val (fn [p]
                  (Math/abs (- prior p)))
        old-diff (abs-val sp) 
        new-diff (abs-val p)
        np-a (assoc a :sp p)]
    (cond
      (:informed? a) np-a
      (< new-diff old-diff) np-a
      :else (assoc np-a :prior (/ (+ prior p) 2)))))

;; Market functions
(defn make-agent-list
  [inf-prior start-price]
  (into []
        (concat (take (* agent-num inform-frac)
                      (repeatedly (partial make-agent start-price inf-prior)))
                (take (* (- 1 inform-frac) agent-num)
                      (repeatedly (partial make-agent start-price)))))
  )
(defn make-market
  []
  (let [security (draw-round)
        start-price (draw-round)]
    {:security security
     :price start-price
     :agents (make-agent-list security start-price)}
    ))
(defn- update-price
  [m pe]
  (+ (:price m) (cond
                  (> pe 0) 0.01
                  (< pe 0) -0.01
                  :else 0))
  ;(+ (* pe 0.01) (:price m))
  )
(defn take-order
  [a p]
  (let [{prior :prior id :id} a]
    (cond
      (> p prior) {id :sl}
      (< p prior) {id :bl}
      :else nil)))
(defn- take-orders
  [al p]
  (let [take-order (fn [a]
                     (let [{prior :prior id :id} a]
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
  (let [[new-agents pe] (take-orders (map #(agent-learn % (:price m)) (:agents m)) (:price m))]
    (-> m
        (assoc :agents new-agents)
        (assoc :price (update-price m pe))
        ))
  )

;; Oz
(defn make-data
  [ms]
  (vec (mapcat #(let [priors (mapv :prior (:agents %2))
                      infs (mapv :informed? (:agents %2))]
                  (mapv (fn [prior inf] (hash-map :time %1
                                                  :price (:price %2)
                                                  :prior prior
                                                  :informed? inf)) priors infs))
               (range (count ms)) ms))
  )
(def m (make-market))
(def mt (make-data (take 100 (iterate market-update m))))
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
(oz/start-server!)
(oz/view! price-plot)

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
