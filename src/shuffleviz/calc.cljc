(ns shuffleviz.calc)

(defn transform [coll ordering]
  (let [coll (vec coll)]
    (mapv (fn [i] (get coll i)) ordering)))

#_(transform [:a :b :c] [2 1 0])

#_(reductions transform [[0 1 2] [2 1 0] [2 1 0]])

(defn calculate-distributions [trials]
  (let [N (count (first trials))]
    (->> trials
         ;; [ [0 2 1] [2 0 1] ]
         (mapcat (fn [s]
                   (map-indexed (fn [i x]
                                  [i x]) s)))
         ;; ([0 0] [1 2] [2 1] [0 2] [1 0] [2 1])
         (reduce (fn [memo [pos n]]
                   (update memo n (fnil conj []) pos)) {})
         ;; {0 [0 1], 2 [1 0], 1 [2 2]}
         (map (fn [[in positions]]
                [in (->> (frequencies positions)
                         #_(map (fn [[position cnt]]
                                  [position (/ cnt (count trials))]) )
                         #_(into {}))]))
         (into {})
         ;; X in position Y N times
         ;; {0 {0 1, 1 1}, 2 {1 1, 0 2}, 1 {2 2}}

         ;; {0 {0 1/2, 1 1/2}, 2 {1 1/2, 0 1/2}, 1 {2 1}}
         )))


#_(defn error [unshuffled shuffles]
  (let [n-trials (count shuffles)
        n-cards (count unshuffled)
        target (/ n-trials n-cards)
        distributions (calculate-distributions shuffles)]
    (when (seq distributions)
      (/ (js/Math.round (* 100 (reduce + (for [i unshuffled
                                               j unshuffled]
                                           (/ (js/Math.pow (- (get-in distributions [i j] 0) target)
                                                            2)
                                              target)))))
         100))))
