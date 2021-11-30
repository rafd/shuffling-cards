(ns shuffleviz.ui.matrix
  (:require
    [reagent.core :as r]
    [shuffleviz.calc :as calc]
    [shuffleviz.misc :refer [color-scale ->interpolate-deviation]]))

(defn table-canvas
  [unshuffled shuffles]
  (let [table-target-size-px 208
        cell-size-px (Math/floor (/ table-target-size-px (count unshuffled)))
        table-actual-size-px (* cell-size-px (count unshuffled))
        ;; distributions is: X in position Y N times
        ;; {0 {0 1, 1 1}, 2 {1 1, 0 2}, 1 {2 2}}
        distributions (calc/calculate-distributions shuffles)]
    (if (empty? distributions)
      [:div {:style {:width table-actual-size-px
                     :height table-actual-size-px
                     :background "grey"}}]
      [:canvas {:width table-actual-size-px
                :height table-actual-size-px
                :ref
                (fn [canvas]
                  (when canvas
                    (let [context (.getContext canvas "2d")]
                      (doseq [i unshuffled
                              o unshuffled]
                        (set! (.-fillStyle context)
                              (color-scale
                                (->interpolate-deviation
                                  (get-in distributions [i o] 0)
                                  (count unshuffled)
                                  (count shuffles))))
                        (.fillRect context
                                   (* cell-size-px i)
                                   (- table-actual-size-px (* cell-size-px (inc o)))
                                   cell-size-px
                                   cell-size-px)))))}])))

(defn table-svg
  [unshuffled shuffles]
  (let [table-target-size-px 208
        cell-size-px (Math/floor (/ table-target-size-px (count unshuffled)))
        table-actual-size-px (* cell-size-px (count unshuffled))
        ;; distributions is: X in position Y N times
        ;; {0 {0 1, 1 1}, 2 {1 1, 0 2}, 1 {2 2}}
        distributions (calc/calculate-distributions shuffles)]
    [:svg {:style {:width table-actual-size-px
                   :height table-actual-size-px
                   :background "gray"}}
     (when (seq distributions)
       (for [i unshuffled
             j unshuffled]
         ^{:key [i j]}
         [:rect {:x (* cell-size-px i)
                 :y (* cell-size-px j)
                 :fill (color-scale
                         (->interpolate-deviation
                           (get-in distributions [i j] 0)
                           (count unshuffled)
                           (count shuffles)))
                 :height cell-size-px
                 :width cell-size-px}]))]))

(defn table-html
  [unshuffled shuffles]
  (let [table-target-size-px 208
        cell-size-px (Math/floor (/ table-target-size-px (count unshuffled)))
        table-actual-size-px (* cell-size-px (count unshuffled))
        ;; distributions is: X in position Y N times
        ;; {0 {0 1, 1 1}, 2 {1 1, 0 2}, 1 {2 2}}
        distributions (calc/calculate-distributions shuffles)]
    (if (empty? distributions)
      [:div {:style {:width table-actual-size-px
                     :height table-actual-size-px
                     :background "gray"}}]
      [:table {:cellSpacing 0
               :cellPadding 0}
       [:tbody
        (for [i unshuffled]
          ^{:key i}
          [:tr
           (for [j unshuffled]
             ^{:key j}
             [:td
              (let [actual (get-in distributions [i j] 0)]
                [:div {:style {:background (color-scale (->interpolate-deviation actual (count unshuffled) (count shuffles)))
                               :width (str cell-size-px "px")
                               :height (str cell-size-px "px")}}])])])]])))

;; input on bottom; output on left
(defn table [unshuffled shuffles]
  [:div {:style {:margin-left "5px"}}
   [:div {:style {:display "flex"}}
    [:div {:style {:display "flex"
                   :width "30px"
                   :padding-right "3px"
                   :text-align "right"
                   :flex-direction "column"
                   :justify-content "space-between"}}
     [:div (dec (count unshuffled))]
     [:div "OUT"]
     [:div "0"]]
    ;; table-canvas is by far the fastest
    [table-canvas unshuffled shuffles]]
   [:div {:style {:display "flex"
                  :margin-left "32px"
                  :justify-content "space-between"}}
    [:div "0"]
    [:div "IN"]
    [:div (dec (count unshuffled))]]
   (when (< 1 (count shuffles))
     [:div {:style {:text-align "center"
                    :margin-left "32px"}}
      (count (frequencies shuffles)) "/" (count shuffles)])])


(defn stateful-table-view
  [in repeat-times shuffle-fn animated?]
  (let [shuffles (r/atom [])]
    (fn [in repeat-times shuffle-fn animated?]
      [:section {:on-click
                 (fn []
                   (if animated?
                     (let [calc (fn calc []
                                  (swap! shuffles conj (shuffle-fn in))
                                  (when (< (count @shuffles) repeat-times)
                                    (js/requestAnimationFrame calc)))]
                       (reset! shuffles [])
                       (calc))
                     (reset! shuffles (repeatedly repeat-times #(shuffle-fn in)))))}
       #_[:h1 {:style {:text-align "center"
                     :margin "0"}}
        (count @shuffles) "/" repeat-times]
       [table in @shuffles]])))
