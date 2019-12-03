(ns trajers.core
  (:gen-class)
  (:require [oz.core :as oz]
            [incanter.stats :as stats]
            )
  )

;; Constants
;; (def agent-num 500)
;; (def theta [0 1])
;; (def theta-weight 1)
;; (def epsilon [0 1])
;; (def epsilon-weight 1)

;; (def norm-mean 0)
;; (def norm-std 1)
;; (def inform-frac 0.6)

;; Utilities
;; (defn round2
;;   "Round a double to the given precision (number of significant digits)"
;;   [precision d]
;;   (let [factor (Math/pow 10 precision)]
;;     (/ (Math/round (* d factor)) factor)))
;; (defn uuid
;;   []
;;   (keyword (str (java.util.UUID/randomUUID))))
(defn sample-agent
  ([m]
   (first (:agents m)))
  ([m i]
   (nth (:agents m) i)))
;; (defn converge?
;;   [m]
;;   (some #(< % 3) (map (comp count frequencies) (partition 10 1 (:sp m))))
;;   )
;; (defn average
;;   [xs]
;;   (float (/ (apply + xs) (count xs))))
;; (defn informed-agent?
;;   [a]
;;   (some :informed? (:prior a)))
(defn write-object
  "Serializes an object to disk so it can be opened again later.
   Careful: It will overwrite an existing file at file-path."
  [obj file-path]
  (with-open [wr (clojure.java.io/writer file-path)]
    (.write wr (pr-str obj))))

;; Draw functions
;; (defn draw-incant-norm
;;   ([]
;;    (stats/sample-normal 1 :mean norm-mean :sd norm-std))
;;   ([n]
;;    (stats/sample-normal n :mean norm-mean :sd norm-std))
;;   ([m sd]
;;    (stats/sample-normal 1 :mean m :sd sd)))
;; (defn draw-round
;;   []
;;   (round2 2 (draw-incant-norm)))
;; (defn draw
;;   [v]
;;   (apply draw-incant-norm v)
;;   )
;; (defn draw-u
;;   []
;;   (+ (* theta-weight (draw theta) (* epsilon-weight (draw epsilon))))
;;   )

;; Agent functions
;; (defn prior-vals
;;   [a]
;;   (mapv :value (:prior a)))
;; (defn does-not-move-toward?
;;   [old-price new-price prior]
;;   (let [abs-val (fn [p] (Math/abs (- prior p)))]
;;     (not (< (abs-val new-price) (abs-val old-price)))
;;     ;(> (abs-val new-price) (abs-val old-price))
;;     )
;;   )
;; (defn avg-update
;;   [p prior]
;;   (/ (+ (:value prior) p) 2))
;; (defn rand-uninf
;;   [v]
;;   (let [r (rand-int (count v))]
;;     (cond
;;       (every? :informed? v) nil
;;       (:informed? (get v r)) (recur v)
;;       :else r)
;;     )
;;   )
;; (defn rand-prior-update
;;   ([a price pred upd]
;;    (let [pri (:prior a)
;;          i (rand-uninf pri)
;;          sel-pri (get pri i)]
;;      (cond
;;        (and sel-pri pred) (assoc a :prior (update pri i #(assoc % :value (upd (- price (- (reduce + (map :value pri)) (:value sel-pri))) sel-pri))))
;;        :else a 
;;        )))
;;   ([a price pred upd v]
;;    (let [pri (:prior a)
;;          i (rand-uninf pri)
;;          sel-pri (get pri i)
;;          social-i (first (sort-by val > (frequencies v)))
;;          social-pri (if social-i (get pri (key social-i)) sel-pri)]
;;      (cond
;;        (and sel-pri pred (informed-agent? a)) [(assoc a :prior (update pri i #(assoc % :value (upd (- price (- (reduce + (map :value pri)) (:value sel-pri))) sel-pri)))) (conj v i)]
;;        (and social-pri pred) [(assoc a :prior (update pri social-i #(assoc % :value (upd (- price (- (reduce + (map :value pri)) (:value social-pri))) social-pri)))) v]
;;        :else [a v] 
;;        ))))
  ;; (defn rand-prior-update
  ;;   [m pred upd]
  ;;   (mapv (fn [a]
  ;;           (let [pred (pred m a)
  ;;                 pri (:prior a)
  ;;                 i ((fn [v]
  ;;                      (let [r (rand-int (count v))]
  ;;                        (cond
  ;;                          (every? :informed? v) nil
  ;;                          (:informed? (get v r)) (recur v)
  ;;                          :else r)
  ;;                        )) pri)
  ;;                 sel-pri (get pri i)]
  ;;             (cond
  ;;               (or (not pred) (informed-agent? a) (not sel-pri)) a
  ;;               :else (let [adjust-price-for-prior (- (:price m) ;; Adjust price for
  ;;                                                     (- (reduce + (map :value pri)) (:value sel-pri))) ;; the parameters that are not selected. This leaves only the part of price due to the selected parameter.
  ;;                           ]
  ;;                       (assoc a :prior (update pri i #(assoc % :value (upd adjust-price-for-prior sel-pri)))))))) (:investors m)))
;; (defn rand-prior-update
;;   ([al last-price current-price pred upd social?]
;;    (loop [al al
;;           new-al []
;;           sm []]
;;      (cond (empty? al) new-al
;;            :else (let [a (first al)
;;                        pred (pred last-price current-price (reduce + (prior-vals a)))
;;                        pri (:prior a)
;;                        i (rand-uninf pri)
;;                        sel-pri (get pri i)
;;                        social-i (if (empty? sm) nil (rand-nth sm)) ;(if-let [mf (first (sort-by val > (frequencies sm)))] (key mf) nil)
;;                        social-pri (if social-i (get pri social-i) sel-pri)
;;                        update-prior (fn [i prior]
;;                                       (assoc a :prior (update pri i #(assoc % :value (upd (- current-price (- (reduce + (map :value pri)) (:value prior))) prior)))))]
;;                    (do
;;                      ;(println sm)
;;                      (recur (rest al)
;;                             (conj new-al (cond
;;                                            (or (not sel-pri) (not pred)) a
;;                                            (and (not (informed-agent? a)) social? social-i) (update-prior social-i social-pri)
;;                                            :else (update-prior i sel-pri)
;;                                            )
;;                                   )
;;                             (cond
;;                               (and (informed-agent? a) pred) (conj sm i)
;;                               :else sm)))
;;                    )))
;;    )
;;   )
;; (defn make-agent
;;   ([n]
;;    {:prior (vec (repeatedly n (fn [] {:informed? false :value (draw-round)}))) 
;;     :id (uuid)})
;;   ([n prior]
;;    (let [valv (vec (repeatedly n (fn [] {:informed? false :value (draw-round)})))
;;          i (rand-int n)]
;;      {:prior (assoc valv i {:informed? true :value (get prior i)}) 
;;       :id (uuid)}))
;;   ([n prior fully?]
;;    {:prior
;;     (mapv (fn [pr] {:informed? true :value pr}) prior)
;;     :id (uuid)})
;;   )
;; Market functions
;; (defn make-agent-list
;;   [n inf-prior]
;;   (into []
;;         (concat (repeatedly (* agent-num inform-frac) (partial make-agent n inf-prior true)) ;; add "true" as third parameter here if you want informed agents to be fully informed.
;;                 (repeatedly (* (- 1 inform-frac) agent-num) (partial make-agent n))))
;;   )
;; (defn make-market
;;   [n]
;;   (let [security (vec (repeatedly n draw-round))
;;         start-price (+ (draw-round) (reduce + security))] ;; this way start price is always a known mean and variance from the "true" value
;;     {:security security
;;      :price start-price
;;      :sp [start-price]
;;      :agents (make-agent-list n security)}
;;     ))
;; (defn- update-price
;;   [p pe]
;;   (+ p (cond
;;          (> pe 0) 0.01
;;          (< pe 0) -0.01
;;          :else 0))
;;   ;(+ (* pe 0.01) (:price m))
;;   )
;; (defn- take-orders
;;   [al p]
;;   (let [take-order (fn [a]
;;                      (let [prior (reduce + (prior-vals a))
;;                            id (:id a)]
;;                        (cond
;;                          (> p prior) {id :sl}
;;                          (< p prior) {id :bl}
;;                          :else nil)))
;;         orders (filter #(not (nil? %)) (mapv #(take-order %) al)) 
;;         [sl bl e] (let [[sl bl] (mapv #(set (keys (into {} %)))
;;                                ((juxt filter remove) #(= :sl (val (first %))) orders))
;;                         blcount (count bl)
;;                         slcount (count sl)
;;                         e (- blcount slcount)]
;;                     (conj (cond
;;                              (> slcount blcount) [(set (take blcount sl)) bl]
;;                              (> blcount slcount) [sl (set (take slcount bl))]
;;                              :else [sl bl])
;;                           e))
;;         add-stock (fn [a s]
;;                     (cond
;;                       (vector? (:holdings a)) (assoc a :holdings (conj (:holdings a) s))
;;                       :else (assoc a :holdings [s])))
;;          ]
;;     [(mapv (fn [a] (cond
;;                       (contains? sl (:id a)) (add-stock a p)
;;                       (contains? bl (:id a)) (add-stock a (- p))
;;                       :else a)) al) e]
;;     )
;;   )

;; (defn market-update
;;   [m]
;;   (let [price (:price m)
;;         sp (:sp m)
;;         [new-agents pe] (take-orders (rand-prior-update (:agents m) (last sp) price does-not-move-toward? avg-update false) price)]
;;     (-> m
;;         (assoc :sp (conj sp price))
;;         (assoc :agents new-agents)
;;         (assoc :price (update-price price pe))
;;         ))
;;   )

;; Oz
;; (defn make-data
;;   [ms]
;;   (vec (mapcat #(let [priors (mapv (fn [a] (reduce + (map :value (:prior a)))) (:agents %2))
;;                       infs (mapv (fn [a] (:informed? (first (:prior a)))) (:agents %2))]
;;                   (mapv (fn [prior inf] (hash-map :time %1
;;                                                   :price (:price %2)
;;                                                   :security (reduce + (:security %2))
;;                                                   :prior prior
;;                                                   :informed? inf)) priors infs))
;;                (range (count ms)) ms))
;;   )
;; (defn make-snap-data
;;   [ms]
;;   (vec (mapcat #(let [priors (mapv (fn [a] (prior-vals a)) (:agents %2))
;;                       infs (mapv (fn [a] (mapv :informed? (:prior a))) (:agents %2))]
;;                   (mapv (fn [prior inf] (hash-map :time %1
;;                                                   :price1 (first (:security %2)) 
;;                                                   :price2 (second (:security %2)) 
;;                                                   :prior1 (first prior)
;;                                                   :prior2 (second prior)
;;                                                   :informed? (if (first inf) true false)
;;                                                   )) priors infs))
;;                (range (count ms)) ms)))
;; (defn iterate-markets
;;   [s n]
;;   (map #(take-while (comp not converge?) (iterate market-update %)) (take n (repeatedly #(make-market s)))))
;; (defn market-convergence-numbers
;;   ([v]
;;    (market-convergence-numbers v 500))
;;   ([v n]
;;    ;;(pmap #(average (mapv count (iterate-markets % n))) v)
;;    (pmap #(mapv count (iterate-markets % n)) v)
;;    ))
;; (defn mispricing-numbers
;;   [n]
;;   (vec (pmap #(- (:price (last %)) (reduce + (:security (last %)))) (iterate-markets 2 n))))
;; (defn sd-priors
;;   [m]
;;   (stats/sd (map #(get-in % [:prior 0 :value]) (filter #(not (informed-agent? %)) (:agents m)))))
;; (defn prior-error
;;   [m]
;;   (/ (reduce + (map #(Math/abs (- (get-in % [:prior 0 :value]) (get (:security m) 0))) (filter #(not (informed-agent? %)) (:agents m)))) (count (:agents m))))

;; ;; Get stdevs
;; (map #(sd-priors (last (first (iterate-markets % 1)))) (map inc (range 10)))
;; (map (fn [i] (/ (reduce + (map #(sd-priors (last %)) (iterate-markets i 100))) 100)) (map inc (range 10)))

;; Scratch on sd
;; (defn sd-error
;;   [m]
;;   (stats/sd (map #(- %1 %2) (:security m) (avg-prior m))))
;; (def errors-by-size (mapv (fn [s] (map sd-error (map last (iterate-markets s 500)))) (rest (map inc (range 10)))))
  ;; (defn sse
  ;;   [m]
  ;;   (reduce + (map #(* (- %1 %2) (- %1 %2)) (:security m) (avg-prior m))))
  ;; (defn sse-avg
  ;;   [m]
  ;;   (/ (reduce + (map #(* (- %1 %2) (- %1 %2)) (:security m) (avg-prior m))) (count (:security m))))
  ;; (def sse-by-size (mapv (fn [s]
  ;;                             (map sse
  ;;                                  (map last (iterate-markets s 500))))
  ;;                           (rest (map inc (range 10)))))
  ;; (def sse-avg-by-size (mapv (fn [s]
  ;;                             (map sse-avg
  ;;                                  (map last (iterate-markets s 500))))
  ;;                           (rest (map inc (range 10)))))

;; (def m (make-market 1))
;(def mkt-it (take 500 (iterate market-update m)))
;; (def mkt-it (take-while (comp not converge?) (iterate market-update m)))
;; (def mt (make-data mkt-it))
;; (def snap (make-snap-data mkt-it))
;; (def price-plot
;;   {:height 600
;;    :width 800
;;    :data {:values mt}
;;    :layer [{:encoding {:x {:field "time"}
;;                        :y {:field "security"}}
;;             :mark {:type "line"}}
;;            {:encoding {:x {:field "time"}
;;                        :y {:field "price"}}
;;             :mark {:type "line"}}
;;            {:encoding {:x {:field "time"}
;;                        :y {:field "prior"}
;;                        :color {:field "informed?"}}
;;             :mark {:type "point"}}]
;;    })
;; (def mst (filter #(= (:time %) 599) snap))
;; (def price-snap-plot
;;   {:height 600
;;    :width 800
;;    :data {:values mst}
;;    :layer [{:encoding {:x {:field "prior1"}
;;                        :y {:field "prior2"}
;;                        :color {:field "informed?"}}
;;             :mark {:type "point" :opacity 0.3}}
;;            {:encoding {:x {:field "price1"}
;;                        :y {:field "price2"}}
;;             :mark {:type "point" :shape "square" :size 60}}]
;;    })
;; (defn plot-snap
;;   [d n]
;;   (oz/view! {:height 600
;;    :width 800
;;    :data {:values (filter #(= (:time %) n) d)}
;;    :layer [{:encoding {:x {:field "prior1"}
;;                        :y {:field "prior2"}
;;                        :color {:field "informed?"}}
;;             :mark {:type "point" :opacity 0.3}}
;;            {:encoding {:x {:field "price1"}
;;                        :y {:field "price2"}}
;;             :mark {:type "point" :shape "square" :size 60}}]
;;    }))
;; ;(oz/view! plot-snap)
;; ;(oz/start-server!)

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
