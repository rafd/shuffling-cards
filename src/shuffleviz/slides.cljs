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
   #_{:name "identity"
    :f identity
    :perfect? true}
   #_{:name "reverse"
    :f reverse
    :perfect? true}

   {:name "Overhand Shuffle (aka Hindu Shuffle)"
    :f shuffles/overhand-shuffle
    :image "overhand.png"}

   {:name "Middle Pull Shuffle"
    :f shuffles/middle-pull-alternating-shuffle}

   #_{:name "Fisher-Yates Shuffle (Clojure, Java, etc.)"
    :f shuffle}

   #_{:name "Perfect Pluck"
    :f shuffles/pick-one-at-a-time-shuffle
    :repeat-pointless? true}
   #_{:name "Biased Pluck"
    :f shuffles/biased-pick-one-at-a-time-shuffle
    :repeat-pointless? true}

   {:name "Milk Shuffle"
    :f shuffles/milk-shuffle
    :perfect? true
    :image "milk.png"}
   {:name "Sloppy Milk Shuffle"
    :f shuffles/sloppy-milk-shuffle}



   #_{:name "Raf's Shuffle"
    :f shuffles/raf-shuffle
    :perfect? true}
   #_{:name "Sloppy Raf's Shuffle"
    :f shuffles/sloppy-raf-shuffle}

   {:name "Perfect Riffle Shuffle (aka Dovetail, Faro)"
    :f shuffles/perfect-interleave-shuffle
    :perfect? true
    :image "perfect-riffle.png"}
   {:name "Near-Perfect Riffle Shuffle"
    :f shuffles/interleave-shuffle
    :image "riffle.png"}
   {:name "Near-Perfect Riffle Shuffle (64 cards)"
    :f shuffles/interleave-shuffle
    :card-count 64}
   {:name "Sloppy Riffle Shuffle"
    :f shuffles/sloppy-interleave-shuffle}
   #_{:name "Sloppy Riffle Shuffle (24 cards)"
    :f (comp shuffles/sloppy-interleave-shuffle shuffles/middle-pull-alternating-shuffle)
    :card-count 24}])

#_(defn stateful-table-view
  [_]
  (let [shuffles (r/atom [])]
    (fn [{:keys [in repeat-times reapply-times shuffle-fn]}]
      [:section {:on-click
                 (fn []
                   (swap! shuffles concat (repeatedly repeat-times #(shuffles/reapply shuffle-fn reapply-times in))))}
       [:div {:style {:text-align "center"
                      :font-weight "bold"}}
        reapply-times "x"]
       [matrix/table in @shuffles]])))

(defn slide-view
  [_]
  (let [data (r/atom {:singles []})]
    (fn [{:keys [name f image repeat-pointless? card-count]}]
      (let [in-cards (range (or card-count 52))
            columns (if repeat-pointless?
                      (range 1)
                      (range 8))
            compute! (fn []
                       (let [singles (mapv (fn [_]
                                             (f in-cards))
                                           columns)
                             reapplies (vec (reductions calc/transform singles))
                             big-calc (fn big-calc [column]
                                        (swap! data update-in [:repeats column] concat
                                               (repeatedly 1000 #(shuffles/reapply f (inc column) in-cards)))
                                        (when (< column (dec (count columns)))
                                          (js/requestAnimationFrame #(big-calc (inc column)))))]
                         (swap! data assoc
                                :singles singles
                                :reapplies reapplies)
                         (js/requestAnimationFrame #(big-calc 0))))]
        [:div {:style {:margin-top "10em"}}
         #_(when image
           [:div {:style {:text-align "center"}}
            [:img {:src image}]])
         [:h1 {:style {:font-size "3.5em"
                       :text-align "center"}}
          name
          [:button {:on-click (fn [] (compute!))} "Compute"]]
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
                                 (swap! data update-in [:repeats column] concat (repeatedly 1000 #(shuffles/reapply f (inc column) in-cards))))}

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

(defn understanding-viz-view []
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
       [stateful-table-view in-cards 1 shuffles/pure-rand-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "riffle"]
       [stateful-table-view in-cards 1 shuffles/perfect-interleave-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "overhand"]
       [stateful-table-view in-cards 1 shuffles/overhand-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "fisher-yates"]
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
       [stateful-table-view in-cards 1000 shuffles/pure-rand-cut]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/perfect-interleave-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffles/overhand-shuffle]]
      [:div
       [:h1 {:style {:text-align "center"}} "x1000"]
       [stateful-table-view in-cards 1000 shuffle]]]


     [:div {:style {;:display "flex"
                    :background "black"
                    :margin-top "2em"
                    :text-align "center"}}
      [:img {:src "/riffle.png" :style {:vertical-align "top"} :width "30%"}]
      [:img {:src "/overhand.png" :width "30%"}]]]))

(defn slides-view []
  [:div {:style {:padding-top "2em"}}
   [legend-view]
   [understanding-viz-view]
   (for [s shuffles]
     ^{:key (:name s)}
     [slide-view s])
   [:div {:style {:margin-top "20em"
                  :padding-bottom "10em"
                  :display "flex"
                  :justify-content "center"}}
    [stateful-table-view (range 52) 100 (fn [in]
                                           (shuffles/reapply shuffles/raf-shuffle (rand-int (/ 52 2)) in))]]])
