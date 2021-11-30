(ns shuffleviz.shuffles)

;; HELPERS

(defn clamp
  [a b x]
  (cond
    (< a x b) x
    (< b x) b
    :else a))

(defn random-gaussian
  "Generates a number from a normal distribution with given mean and standard deviation.

  Based on: https://github.com/ashenfad/cljx-sampling/blob/c1540f22444dc7ce31da8658cc07a996e52326ad/src/cljx/cljx_sampling/random.cljx#L64"
  [mean std-dev]
  ;; Uses the Marsaglia polar method, but returns only one sample.
  ;; http://en.wikipedia.org/wiki/Marsaglia_polar_method
  (let [x (dec (rand 2))
        y (dec (rand 2))
        s (+ (* x x) (* y y))]
    (if (or (>= s 1) (zero? s))
      (recur mean std-dev)
      (+ (or mean 0)
         (* x
            (or std-dev 1)
            (Math/sqrt (* -2 (/ (Math/log s) s))))))))

(defn fuzzy-gauss [n]
  (Math/round (random-gaussian n 3)))

(defn fuzzy-linear
  "

  =======
  ======="
  [n]
  (+ n
     (int (/ n -4))
     (rand-int (/ n 2))))

(defn fuzzy-triangle
  "Given N, returns N +/- 25% (random); generates two random numbers adds one and subtracts the other

     =
    ===
   =====
  ======="
  [n]
  (let [a (rand-int (/ n 4))
        b (- (rand-int (/ n 4)))]
    (+ n a b)))

(def fuzzy fuzzy-gauss)

(comment (let [f 'fuzzy-gauss
               xs (repeatedly 10000 (fn [] ((eval f) 26)))
               min-x (apply min xs)]
           (->> xs
                (frequencies)
                (sort-by first)
                (map (fn [[x count]]
                       (str x "\t" (apply str (repeat (int (/ count min-x)) "=")))))
                (clojure.string/join "\n")
                (spit (str "histogram-" (name f) ".txt")))))

(defn fuzzy-half
  [n]
  (fuzzy (Math/round (/ n 2))))

(defn half
  [n]
  (Math/round (/ n 2)))

(defn cut [n coll]
  (concat
    (take-last (- (count coll) n) coll)
    (take n coll)))

;; SHUFFLES

(defn perfect-cut [coll]
  "Cut deck once, exactly in half"
  (let [n (half (count coll))]
    (cut n coll)))

(defn pure-rand-cut
  "Cut deck once, location of cut random across entire deck"
  [coll]
  (let [n (rand-int (count coll))]
    (cut n coll)))

(defn middle-bias-rand-cut
  "Cut deck once, location of cut in middle +- 25% (random)"
  [coll]
  (let [n (fuzzy-half (count coll))]
    (cut n coll)))

(defn multi-cut
  "Cut deck into approximately N groups"
  [n coll]
  (loop [new-deck []
         origin-deck coll]
    (if (seq origin-deck)
      (let [i (fuzzy (/ (count coll) n))
            top? (rand-nth [true false])]
        (recur (if top?
                 (concat (take i origin-deck)
                         new-deck)
                 (concat new-deck
                         (take i origin-deck)))
               (drop i origin-deck)))
      new-deck)))

(defn hindu-shuffle
  "Move appx 5 cards from top of deck to top of new deck, until done"
  [coll]
  (loop [new-deck []
         origin-deck (vec coll)]
    (if (seq origin-deck)
      (let [n (fuzzy 5)]
        (recur (concat (take n origin-deck)
                       new-deck)
               (drop n origin-deck)))
      new-deck)))

(def overhand-shuffle hindu-shuffle)

(defn pick-one-at-a-time-shuffle
  "Pick one card randomly from starter-deck and place on top of new-deck"
  [coll]
  (loop [origin-deck coll
         new-deck []]
    (if (seq origin-deck)
      ;; could be faster with random index and subvec
      (let [v (rand-nth origin-deck)]
        (recur
          (remove (fn [x] (= v x)) origin-deck)
          (conj new-deck v)))
      new-deck)))

(defn biased-pick-one-at-a-time-shuffle
  "Pick one card from near the middle of starter-deck and place on top of new-deck"
  [coll]
  (loop [origin-deck coll
         new-deck []]
    (if (seq origin-deck)
      (let [N (count origin-deck)
            n (Math/round (random-gaussian (int (/ N 2))
                                           (int (/ N 4))))
            v (get (vec origin-deck) n)]
        ;; could be faster with subvec
        (recur
          (remove (fn [x] (= v x)) origin-deck)
          (conj new-deck v)))
      new-deck)))

(defn interleave-shuffle
  "Cut deck in half, then interleave perfectly; but which half is on top depends"
  [coll]
  (let [n (half (count coll))
        [a b] (split-at n coll)
        [a b] (rand-nth [[a b] [b a]])]
    (concat (interleave a b)
            (drop (count b) a)
            (drop (count a) b))))

(defn perfect-interleave-shuffle
  [coll]
  (let [n (half (count coll))
        [a b] (split-at n coll)]
    (concat (interleave a b)
            (drop (count b) a)
            (drop (count a) b))))

(defn sloppy-interleave-shuffle
  "Cut deck approximately in half, then interleave with 1-3 cards from each side"
  [coll]
  (let [n (clamp 0 (dec (count coll))
                 (+ (int (/ (count coll) 2))
                    (rand-nth [-2 -1 0 1 2])))
        [a b] (split-at n coll)
        [a b] (rand-nth [[a b] [b a]])]
    (loop [new-deck []
           origin-deck-a a
           origin-deck-b b]
      (if (or (seq origin-deck-a)
              (seq origin-deck-b))
        (let [cnt-a (inc (rand-int 2))
              cnt-b (inc (rand-int 2))]
          (recur (concat new-deck
                         (take cnt-a origin-deck-a)
                         (take cnt-b origin-deck-b))
                 (drop cnt-a origin-deck-a)
                 (drop cnt-b origin-deck-b)))
        new-deck))))

(defn sloppy-milk-shuffle
  "Move 1-2 from top and 1-2 bottom cards from deck onto new pile, repeat until original deck empty"
  [coll]
  (loop [start-deck (vec coll)
         new-deck []]
    (case (count start-deck)
      0
      new-deck
      1
      (concat start-deck new-deck)
      2
      (concat start-deck new-deck)
      ;else
      (let [top-n (min (rand-nth [1 2]) (count start-deck))
            bottom-n (min (rand-nth [1 2]) (- (count start-deck) top-n))]
        (recur (subvec start-deck top-n (- (count start-deck) bottom-n))
               (concat
                 (take top-n start-deck)
                 (take-last bottom-n start-deck)
                 new-deck))))))

(defn milk-shuffle
  "Move top and bottom card from deck onto new pile, repeat until original deck empty"
  [coll]
  (loop [start-deck (vec coll)
         new-deck []]
    (case (count start-deck)
      0
      new-deck
      1
      (concat start-deck new-deck)
      ;else
      (recur (subvec start-deck 1 (- (count start-deck) 1))
             (concat
               [(first start-deck)
                (last start-deck)]
               new-deck)))))

(defn raf-shuffle
  "Like milk-shuffle, but after moving top-and-bottom, add bottom of source deck to bottom of new deck"
  [coll]
  (loop [start-deck (vec coll)
         new-deck []]
    (case (count start-deck)
      0
      new-deck
      1
      (concat start-deck new-deck)
      2
      (concat [(first start-deck)] new-deck [(last start-deck)])
      ;else
      (recur (subvec start-deck 1 (- (count start-deck) 2))
             (concat
               [(first start-deck)
                (last start-deck)]
               new-deck
               [(nth start-deck (- (count start-deck) 2))])))))

(defn sloppy-raf-shuffle
  [coll]
  (loop [start-deck (vec coll)
         new-deck []]
    (case (count start-deck)
      0
      new-deck
      1
      (concat start-deck new-deck)
      2
      (concat [(first start-deck)] new-deck [(last start-deck)])
      3
      (concat [(first start-deck) (last start-deck)] new-deck [(second start-deck)])
      4
      (concat [(first start-deck) (last start-deck)] new-deck [(second start-deck)])
      ;else
      (let [top-n (rand-nth [1 2])
            bottom-n (rand-nth [1 2])]
        (recur (subvec start-deck
                       top-n
                       (- (count start-deck) bottom-n 1))
               (concat
                 (take top-n start-deck)
                 (butlast (take-last (inc bottom-n) start-deck))
                 new-deck
                 [(last start-deck)]))))))

(defn middle-pull-top-shuffle
  "Grab appx middle half of deck, then put on top of remaining
   |----A---|--------B-------|----C----|
     ->
   |---------B--------|---A---|----C---|
  "
  [coll]
  (let [n (fuzzy-half (count coll))
        b (- (count coll) n)
        a (fuzzy-half n)
        c (- n a)]
    (concat (take b (drop a coll))
            (take a coll)
            (take-last c coll))))

(defn middle-pull-bottom-shuffle
  "Grab appx middle half of deck, then put on bottom of remaining
   |----A---|--------B-------|----C----|
     ->
   |---A---|----C---|---------B--------|"
  [coll]
  (let [n (fuzzy-half (count coll))
        b (- (count coll) n)
        a (fuzzy-half n)
        c (- n a)]
    (concat (take a coll)
            (take-last c coll)
            (take b (drop a coll)))))

(defn middle-pull-alternating-shuffle
  "Do both a middle-pull-top and middle-pull-bottom"
  [coll]
  (middle-pull-bottom-shuffle (middle-pull-top-shuffle coll)))


;; UTILITIES


(defn split
  "Split coll into n groups, with size of groups near within 1 of average"
  [n coll]
  (let [target-size (quot (count coll) n)
        leftover-count (- (count coll)
                          (* target-size n))
        in-smaller-groups-count (- (count coll)
                                   (* (inc target-size) leftover-count))]
    (concat
      (partition target-size (take in-smaller-groups-count coll))
      (partition-all (inc target-size)
                     (drop in-smaller-groups-count coll)))))

(defn split-and-shuffle
  "Perform f on n piles of coll, then recombine"
  [f n coll]
  (let [decks (split n coll)]
    (->> decks
         (map f)
         (apply concat)
         vec)))

(defn reapply
  "Perform f on coll n times"
  [f n coll]
  ((apply comp (repeat n f)) coll))

#_(split-and-shuffle reverse 3 [1 2 3 4 5 6])
