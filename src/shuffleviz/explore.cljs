(ns shuffleviz.explore
  (:require
    [garden.core :as garden]
    [reagent.core :as r]
    [shuffleviz.calc :as calc]
    [shuffleviz.shuffles :as shuffles]
    [shuffleviz.js-shuffles :as js-shuffles]
    [shuffleviz.ui.matrix :refer [stateful-table-view]]
    [shuffleviz.ui.legend :refer [legend-view]]))

(defn mean [coll]
  (/ (reduce + coll)
     (count coll)))

(def reapply shuffles/reapply)

(def split-and-shuffle shuffles/split-and-shuffle)

(defn shuffle-view [label in repeat-times shuffle-fn]
  [:section {:style {:display "flex"
                     :flex-direction "column"
                     :justify-content "flex-end" #_"space-between"
                     :gap "1em"}}
   [:h1 {:style {:text-align "center"
                 :white-space "pre-wrap"
                 :margin 0
                 #_#_:flex-grow 1}} label]
   [stateful-table-view in 1 shuffle-fn false]
   [stateful-table-view in 100 shuffle-fn true]
   [stateful-table-view in repeat-times shuffle-fn false]])

(defn explorations-view []
  (let [N 52
        in (range N)
        repeat-times 1000
        shuffle-fns [;; understand the viz
                     {:label "identity"
                      :fn identity}
                     {:label "reverse"
                      :fn reverse}
                     ;; what we want:
                     {:label "shuffle"
                      :fn shuffle}

                     ;; cuts:
                     {:label "perfect random cut"
                      :fn shuffles/pure-rand-cut} ;; just because looks random x1000, doesn't mean a single time doesn't have patterns
                     {:label "random cut\n(middle bias)"
                      :fn shuffles/middle-bias-rand-cut}
                     #_{:label "triple-cut"
                      :fn (partial shuffles/multi-cut 3)}

                     ;; so: what to look out for:
                     ;;    streaks in the single case
                     ;;    patterns in the monte-carlo case

                     ;; exploring shuffles:

                     ;; INTERLEAVE SHUFFLE (aka. riffle, or dovetail)

                     {:label "perfect interleave\nx1"
                      :fn shuffles/perfect-interleave-shuffle} ;; deterministic!
                     {:label "perfect interleave\nx2"
                      :fn (partial reapply shuffles/perfect-interleave-shuffle 2)}
                     {:label "perfect interleave\nx3"
                      :fn (partial reapply shuffles/perfect-interleave-shuffle 3)}
                     {:label "perfect interleave\nx8"
                      :fn (partial reapply shuffles/perfect-interleave-shuffle 8)} ;; returns to same!


                     ;; for 'shuffling', being sloppy is much better instead
                     {:label "perfect interleave\nx1"
                      :fn shuffles/perfect-interleave-shuffle}
                     {:label "rand cut interleave \nx1"
                      :fn (partial reapply shuffles/interleave-shuffle 1)}

                     ;; how many interleaves?
                     ;;   thinking of bottom card:
                     ;;       after 1 shuffle: 1/2 chance it's on bottom
                     ;;       after 2        : 1/4
                     ;;   if want < 1/52, need 6 (1/64)

                     {:label "rand cut interleave \nx2"
                      :fn (partial reapply shuffles/interleave-shuffle 2)}
                     {:label "rand cut interleave \nx3"
                      :fn (partial reapply shuffles/interleave-shuffle 3)}
                     {:label "rand cut interleave \nx4"
                      :fn (partial reapply shuffles/interleave-shuffle 4)}
                     {:label "rand cut interleave \nx5"
                      :fn (partial reapply shuffles/interleave-shuffle 5)}
                     {:label "rand cut interleave \nx6"
                      :fn (partial reapply shuffles/interleave-shuffle 6)}


                     ;; in reality, a bit sloppy
                     ;;   MATH guys say 7 times (b/c of biased cuts)
                     {:label "rand cut interleave \nx1"
                      :fn (partial reapply shuffles/interleave-shuffle 1)}
                     {:label "sloppy interleave \nx1"
                      :fn shuffles/sloppy-interleave-shuffle}
                     {:label "sloppy interleave\nx2"
                      :fn (partial reapply shuffles/sloppy-interleave-shuffle 2)}
                     {:label "sloppy interleave\nx3"

                      :fn (partial reapply shuffles/sloppy-interleave-shuffle 3)}
                     {:label "sloppy interleave\nx4"
                      :fn (partial reapply shuffles/sloppy-interleave-shuffle 4)}
                     {:label "sloppy interleave\nx5"
                      :fn (partial reapply shuffles/sloppy-interleave-shuffle 5)} ;; still "top" unshuffled
                     {:label "sloppy interleave\nx6"
                      :fn (partial reapply shuffles/sloppy-interleave-shuffle 6)} ;; almost good enough
                     {:label "sloppy interleave\nx7"
                      :fn (partial reapply shuffles/sloppy-interleave-shuffle 7)}


                     ;; what about cut + interleave
                     ;; can we reduce # of times through deck by introducting a little randomness?
                     {:label "(cut + interleave)\nx1"
                      :fn (partial reapply (fn [x] (shuffles/sloppy-interleave-shuffle (shuffles/middle-bias-rand-cut x))) 1)}
                     {:label "(cut + interleave)\nx2"
                      :fn (partial reapply (fn [x] (shuffles/sloppy-interleave-shuffle (shuffles/middle-bias-rand-cut x))) 2)}
                     ;; cut introduces randomness -- interleave "magnifies" is
                     {:label "(cut + interleave)\nx3"
                      :fn (partial reapply (fn [x] (shuffles/sloppy-interleave-shuffle (shuffles/middle-bias-rand-cut x))) 3)}
                     {:label "(cut + interleave)\nx4"
                      :fn (partial reapply (fn [x] (shuffles/sloppy-interleave-shuffle (shuffles/middle-bias-rand-cut x))) 4)}


                     ;; OVERHAND SHUFFLE (aka hindu-shuffle)

                     ;; classic overhand shuffle,
                     {:label "overhand shuffle\n" ;; (ie. sloppy reverse)
                      :fn shuffles/overhand-shuffle}

                     ;; can repeat
                     {:label "overhand shuffle\nx3"
                      :fn (partial reapply shuffles/overhand-shuffle 3)}
                     {:label "overhand shuffle\nx15"
                      :fn (partial reapply shuffles/overhand-shuffle 15)}
                     {:label "overhand shuffle\nx55"
                      :fn (partial reapply shuffles/overhand-shuffle 55)}

                     ;; cut and repeat
                     {:label "(cut + overhand shuffle)\nx3"
                      :fn (partial reapply (fn [in] (shuffles/overhand-shuffle (shuffles/middle-bias-rand-cut in))) 3)}

                     ;; cut and subshuffle
                     {:label "overhand shuffle 3 piles"
                      :fn (partial split-and-shuffle shuffles/overhand-shuffle 3)}

                     ;; MATH guys say: 10,000



                     ;; canna shuffle:
                     {:label "middle-pull -> top"
                      :fn shuffles/middle-pull-top-shuffle}
                     {:label "middle-pull -> bottom"
                      :fn shuffles/middle-pull-bottom-shuffle}
                     {:label "middle-pull -> top/bottom"
                      :fn (partial reapply shuffles/middle-pull-alternating-shuffle 1)}
                     {:label "middle-pull -> top/bottom\nx5"
                      :fn (partial reapply shuffles/middle-pull-alternating-shuffle 5)}

                     ;; my shuffle
                     ;; milk shuffle:
                     {:label "milk shuffle"
                      :fn shuffles/milk-shuffle}
                     {:label "sloppy milk shuffle"
                      :fn shuffles/sloppy-milk-shuffle}
                     {:label "milk shuffle\nx2"
                      :fn (partial reapply shuffles/milk-shuffle 2)}
                     {:label "milk shuffle\nx3"
                      :fn (partial reapply shuffles/milk-shuffle 3)}
                     {:label "raf's shuffle"
                      :fn shuffles/raf-shuffle}
                     ;;  ^ (decent distribution... but deterministic!)
                     {:label "raf's shuffle\nx2"
                      :fn (partial reapply shuffles/raf-shuffle 2)}
                     ;; cool art though
                     {:label "raf's shuffle\nx rand()"
                      :fn (fn [in]
                            (reapply shuffles/raf-shuffle (rand-int (/ N 2)) in))}

                     {:label "(cut + raf's shuffle)\nx3"
                      :fn (partial reapply (fn [x] (shuffles/raf-shuffle (shuffles/middle-bias-rand-cut x))) 3)}
                     ;; "good shuffles" but impractical
                     {:label "pluck one\n(perfect random)"
                      :fn shuffles/pick-one-at-a-time-shuffle}
                     {:label "pluck one\n(middle biased)"
                      :fn shuffles/biased-pick-one-at-a-time-shuffle}

                     #_{:label "pluck one\n(middle biased)\nx52"
                        :fn (partial reapply shuffles/biased-pick-one-at-a-time-shuffle N)}

                     #_{:label "(cut + pluck one (middle biased))  x52"
                        :fn (partial reapply (fn [in]
                                               (shuffles/biased-pick-one-at-a-time-shuffle (shuffles/middle-bias-rand-cut in))) N)}

                     ;; can chain/combine:
                     {:label "cut\n(middle bias)"
                      :fn shuffles/middle-bias-rand-cut}
                     {:label "overhand shuffle\n(ie. sloppy reverse)"
                      :fn shuffles/overhand-shuffle}
                     {:label "sloppy milk shuffle"
                      :fn shuffles/sloppy-milk-shuffle}
                     {:label "raf's shuffle"
                      :fn shuffles/raf-shuffle}
                     {:label "interleave (sloppy)"
                      :fn shuffles/sloppy-interleave-shuffle}
                     {:label "middle-pull -> top/bottom"
                      :fn shuffles/middle-pull-alternating-shuffle}

                     {:label "explore"
                      :fn (fn [in]
                            (->> in
                                 #_shuffles/middle-bias-rand-cut
                                 #_shuffles/middle-bias-rand-cut
                                 #_shuffles/raf-shuffle
                                 #_shuffles/middle-bias-rand-cut
                                 shuffles/middle-bias-rand-cut
                                 shuffles/sloppy-milk-shuffle
                                 shuffles/middle-bias-rand-cut
                                 shuffles/sloppy-interleave-shuffle
                                 shuffles/middle-bias-rand-cut
                                 shuffles/overhand-shuffle
                                 shuffles/middle-bias-rand-cut
                                 #_shuffles/biased-pick-one-at-a-time-shuffle
                                 #_shuffles/milk-shuffle
                                 #_shuffles/middle-bias-rand-cut
                                 ))}


                     ;; programming shuffles:
                     {:label "code\nrandom->random"
                      :fn js-shuffles/naive-shuffle-random->random}
                     {:label "code\ni->random"
                      :fn js-shuffles/naive-shuffle-i->random}
                     {:label "code\nrandom comparator"
                      :fn js-shuffles/naive-shuffle-random-comparator}
                     {:label "code\nrandom mapping"
                      :fn js-shuffles/naive-shuffle-random-order}
                     {:label "code\nfisher-yates"
                      :fn js-shuffles/shuffle-fisher-yates}

                     ]]
    [:div#app
     [:style
      (garden/css
        [:body
         {:margin 0}
         [:#app
          [:>.legend
           [:>div
            {:text-align "center"
             :min-width "3em"
             :flex-grow 1}]]]])]
     #_[:div.legend2
        [:svg {:width 1000 :height 100 :view-box "0 0 100 200" :preserve-aspect-ratio "none"}
         [:rect {:width 0.1
                 :height 100
                 :x (* 100 (/ 1 N))
                 :y 0
                 :fill "red"}]
         [:rect {:width 100
                 :height 1
                 :x 0
                 :y 50
                 :fill "red"}]
         [:rect {:width 100
                 :height 1
                 :x 0
                 :y 0
                 :fill "red"}]
         [:rect {:width 100
                 :height 1
                 :x 0
                 :y 100
                 :fill "red"}]
         (for [n-actual (range 0 repeat-times)]
           [:rect {:width (* 100 (/ 1 repeat-times))
                   :height 1
                   :x (* 100 (/ n-actual repeat-times))
                   :y (* 100 (->interpolate-deviation n-actual N repeat-times))
                   :fill "black"}])]]
     [legend-view]
     [:div.shuffles {:style {:display "flex"
                             :padding-top "1em"
                             :gap "3em"}}
      (for [{:keys [label fn]} shuffle-fns]
        ^{:key label}
        [shuffle-view label in repeat-times fn])]]))
