(ns shuffleviz.slides
  (:require
    [reagent.core :as r]
    [shuffleviz.calc :as calc]
    [shuffleviz.shuffles :as shuffles]
    [shuffleviz.js-shuffles :as js-shuffles]
    [shuffleviz.ui.matrix :as matrix]
    [shuffleviz.ui.legend :refer [legend-view]]))

(def shuffles
  [#_{:name "naive"
      :f js-shuffles/naive-shuffle-random-comparator}
   {:name "identity"
    :f identity
    :perfect? true}
   {:name "reverse"
    :f reverse
    :perfect? true}

   {:name "perfect cut"
    :f shuffles/perfect-cut
    :perfect? true}
   {:name "middle-biased cut"
    :f shuffles/middle-bias-rand-cut}
   {:name "random cut"
    :f shuffles/pure-rand-cut}

   {:name "Overhand Shuffle (aka Hindu Shuffle)"
    :f shuffles/overhand-shuffle
    :image "diagram-overhand.png"}

   {:name "Middle Pull Shuffle"
    :f shuffles/middle-pull-alternating-shuffle
    :image "diagram-pull.png"}

   #_{:name "Perfect Pluck"
      :f shuffles/pick-one-at-a-time-shuffle
      :repeat-pointless? true}
   #_{:name "Biased Pluck"
      :f shuffles/biased-pick-one-at-a-time-shuffle
      :repeat-pointless? true}

   {:name "Milk Shuffle"
    :f shuffles/milk-shuffle
    :perfect? true
    :image "diagram-milk.png"}
   {:name "Sloppy Milk Shuffle"
    :image "diagram-milk.png"
    :f shuffles/sloppy-milk-shuffle}

   #_{:name "Raf's Shuffle"
      :f shuffles/raf-shuffle
      :perfect? true}
   #_{:name "Sloppy Raf's Shuffle"
      :f shuffles/sloppy-raf-shuffle}

   {:name "Riffle Shuffle"
    :f shuffles/interleave-shuffle
    :image "diagram-riffle.png"}
   {:name "Riffle Shuffle (64 cards)"
    :f shuffles/interleave-shuffle
    :image "diagram-riffle.png"
    :card-count 64}
   {:name "Sloppy Riffle Shuffle"
    :image "diagram-riffle.png"
    :f shuffles/sloppy-interleave-shuffle}

   {:name "Perfect Shuffle"
    :f shuffle}

   {:name "Overhand + Milk + Riffle"
    :f [shuffles/overhand-shuffle
        shuffles/sloppy-milk-shuffle
        shuffles/sloppy-interleave-shuffle]}
   #_{:name "Sloppy Riffle Shuffle (24 cards)"
      :f (comp shuffles/sloppy-interleave-shuffle shuffles/middle-pull-alternating-shuffle)
      :card-count 24}])

(defn slide-view
  [_]
  (let [data (r/atom {:singles []})]
    (fn [{:keys [name f image repeat-pointless? card-count]}]
      (let [in-cards (range (or card-count 52))
            columns (if repeat-pointless?
                      (range 1)
                      (range 8))
            fs (if (fn? f)
                 (take 8 (cycle [f]))
                 (take 8 (cycle f)))
            single-compute (if (fn? f)
                             (fn [f-count]
                               (shuffles/reapply f f-count in-cards))
                             (fn [f-count]
                               (reduce calc/transform (map (fn [f] (f in-cards)) (take f-count fs)))))
            compute-monte-carlo! (fn big-calc [column]
                                   (swap! data update-in [:repeats column] concat
                                          (repeatedly 1000 #(single-compute (inc column))))
                                   (when (< column (dec (count columns)))
                                     (js/requestAnimationFrame #(big-calc (inc column)))))
            compute! (fn []
                       (let [singles (mapv (fn [f]
                                             (f in-cards))
                                           fs)
                             reapplies (vec (reductions calc/transform singles))]
                         (swap! data assoc
                                :singles singles
                                :reapplies reapplies)
                         #_(js/requestAnimationFrame #(compute-monte-carlo! 0))))]
        [:div {:style {:margin-top "10em"}}
         [:h1 {:style {:font-size "3.5em"
                       :text-align "center"}}
          [:img {:src image
                 :style {:vertical-align "middle"
                         :margin-right "0.5em"
                         :height "2em"}}]
          [:span {:style {:vertical-align "middle"}} name]
          [:button {:on-click (fn [] (compute!))} "Compute"]
          [:button {:on-click (fn [] (compute-monte-carlo! 0))} "Monte Carlo"]]
         ;; 1x
         [:div {:style {:display "flex"
                        :justify-content "space-between"}}
          (doall
            (for [column columns]
              ^{:key column}
              [:div
               [:div {:style {:text-align "center"
                              :font-size "2em"
                              :font-weight "bold"}}
                (inc column)]
              [matrix/table in-cards [(get-in @data [:singles column])]]
              [:div {:style {:text-align "center"
                             :margin-left "10px"
                             :font-weight "bold"}} "🠗"]]))]
         ;; 1x -> 8x  (once)
         [:div {:style {:display "flex"
                        :justify-content "space-between"}}
          (doall
            (for [column columns]
              ^{:key column}
              [:div {:style {:position "relative"}}

               [:div {:style {:position "absolute"
                              :font-weight "bold"
                              :top "-0.9em"
                              :right "-1.2em"}} "↗"]
               [matrix/table in-cards [(get-in @data [:reapplies column])]]
               ]))]

         ;; REAPPLY
         [:div {:style {:display "flex"
                        :margin-top "1em"
                        :justify-content "space-between"}}
          (doall
            (for [column columns]
              ^{:key column}
              [:div {:on-click (fn []
                                 (swap! data update-in [:repeats column] concat
                                        (repeatedly 1000 #(single-compute (inc column)))))}

               [matrix/table in-cards (get-in @data [:repeats column])]
               [:div {:style {:text-align "center"
                              :font-size "1.2em"
                              :font-weight "bold"}}
                "x" (count (get-in @data [:repeats column]))]]))]]))))

(defn stateful-table-view [in-cards repeat-times f]
  (let [shuffles (r/atom [])]
    (fn [in-cards repeat-times f]
      [:div {:on-click (fn []
                         (reset! shuffles
                                 (repeatedly repeat-times #(f in-cards))))}
       [matrix/table in-cards @shuffles]])))

(defn understanding-viz-1-view []
  (let [in-cards (range 52)]
    [:div #_{:style {:height "100vh"}}
     [:div {:style {:display "flex"
                    :justify-content "center"}}
      [:div
       [:h1 {:style {:text-align "center"}} "identity"]
       [stateful-table-view in-cards 1 identity]]
      [:div
       [:h1 {:style {:text-align "center"}} "reverse"]
       [stateful-table-view in-cards 1 reverse]]
      [:div
       [:h1 {:style {:text-align "center"}} "cut"]
       [stateful-table-view in-cards 1 shuffles/perfect-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "biased cut"]
       [stateful-table-view in-cards 1 shuffles/middle-bias-rand-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "random cut"]
       [stateful-table-view in-cards 1 shuffles/pure-rand-cut]]
      [:div
       [:h1 {:style {:text-align "center"
                     :position "relative"}} "random"
        [:div {:style {:font-size "0.5em"
                       :position "absolute"
                       :width "100%"}} "(fisher-yates)"]]
       [stateful-table-view in-cards 1 shuffle]]]

     [:div {:style {:display "flex"
                    :justify-content "center"}}
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 identity]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 reverse]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/perfect-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/middle-bias-rand-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/pure-rand-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffle]]]]))

(defn understanding-viz-2-view []
  (let [in-cards (range 52)
        diagram (fn [src]
                  [:img {:src src
                         :height "100px"}])]
    [:div {:style {:margin-top "100px"}}
     [:div {:style {:display "flex"
                    :justify-content "center"}}
      [:div {:style {:text-align "center"}}
       [diagram "diagram-overhand.png"]
       [:h1 {:style {:text-align "}center"}} "overhand"]
       [stateful-table-view in-cards 1 shuffles/overhand-shuffle]]
      [:div {:style {:text-align "center"}}
       [diagram "diagram-milk.png"]
       [:h1 {:style {:text-align "center"}} "milk"]
       [stateful-table-view in-cards 1 shuffles/sloppy-milk-shuffle]]
      [:div {:style {:text-align "center"}}
       [diagram "diagram-pull.png"]
       [:h1 {:style {:text-align "center"}} "pull"]
       [stateful-table-view in-cards 1 shuffles/middle-pull-alternating-shuffle]]
      [:div {:style {:text-align "center"}}
       [diagram "diagram-riffle.png"]
       [:h1 {:style {:text-align "center"}} "perfect riffle"]
       [stateful-table-view in-cards 1 shuffles/interleave-shuffle]]
      [:div {:style {:text-align "center"}}
       [diagram "diagram-riffle.png"]
       [:h1 {:style {:text-align "center"}} "riffle"]
       [stateful-table-view in-cards 1 shuffles/sloppy-interleave-shuffle]]]

     [:div {:style {:display "flex"
                    :justify-content "center"}}
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/overhand-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/sloppy-milk-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/middle-pull-alternating-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/interleave-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/sloppy-interleave-shuffle]]]

     #_[:div {:style {:background "black"
                    :margin-top "2em"
                    :text-align "center"}}
      [:img {:src "/overhand.png" :style {:vertical-align "top"} :width "30%"}]
      [:img {:src "/milk.png" :style {:vertical-align "top"} :width "40%"}]
      [:img {:src "/riffle.png" :style {:vertical-align "top"} :width "30%"}]]]))

(defn slides-view []
  [:div {:style {:padding-top "2em"}}
   [legend-view]
   [understanding-viz-1-view]
   [understanding-viz-2-view]
   (for [s shuffles]
     ^{:key (:name s)}
     [slide-view s])
   [:div {:style {:margin-top "20em"
                  :padding-bottom "10em"
                  :display "flex"
                  :flex-direction "column"
                  :align-items "center"
                  :justify-content "center"}}
    [:h1 "???"]
    [stateful-table-view (range 52) 100 (fn [in]
                                           (shuffles/reapply shuffles/raf-shuffle (rand-int (/ 52 2)) in))]]])
