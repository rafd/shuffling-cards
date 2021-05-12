(ns shuffleviz.misc
  (:require
    [cljsjs.d3]))

(defn color-scale
  ;; x between 0 and 1
  [x]
  (cond
    (= 0 x)
    "black"
    #_(js/d3.interpolateRdBu (- 1 0))
    (= 1 x)
    "#ff00ca"
    #_(js/d3.interpolateRdBu 0)
    :else
    (js/d3.interpolateRdBu (- 1 x #_(+ 0.1 (* 0.8 x))))))

(defn clamp
  [a b x]
  (cond
    (< a x b) x
    (< b x) b
    :else a))

(defn linear-interpolate
  [x [x0 x1] [y0 y1]]
  (let [m (/ (- y1 y0) (- x1 x0))
        b (- y0 (* m x0))]
    (+ (* m x) b)))

(defn ->interpolate-deviation [n-actual n-cards n-trials]
  #_(* 10 (if (< target actual)
          (linear-interpolate actual [target (count shuffles)] [0.5 1])
          (linear-interpolate actual [target (- target (count shuffles))] [0 0.5])))

  (let [target (/ n-trials n-cards)]
    ;; 0 - target        :: 1 - 0
    ;; target - 2*target :: 0 - 1
    #_(clamp 0 1 (if (< n-actual target)
             (linear-interpolate n-actual [0 target] [0 0.5])
             (linear-interpolate n-actual [target (* 2 target)] [0.5 1])))
    ;; 0 - target :: 1 - 0
    ;; target - 1 :: 0 - 1
    #_(if (< n-actual target)
        (linear-interpolate n-actual [0 target] [0 0.5])
        (linear-interpolate n-actual [target n-trials] [0.5 1]))

    #_(if (< n-actual target)
      (linear-interpolate n-actual [(- target n-trials) target] [0 0.5])
      (linear-interpolate n-actual [target n-trials] [0.5 1]))

    #_(if (= n-trials 1)
      n-actual
      (clamp 0 1 (linear-interpolate (js/Math.log n-actual)
               [(js/Math.log (/ target (js/Math.log n-trials)))
                (js/Math.log (* target (js/Math.log n-trials)))]
               [0 1])))

    (if (= n-trials 1)
      n-actual
      (cond
        (= 0 n-actual)
        0
        (= n-trials n-actual)
        1
        :else
        (clamp 0.01 0.99 (linear-interpolate (js/Math.log n-actual)
                           [(js/Math.log (/ target (js/Math.log n-trials)))
                            (js/Math.log (* target (js/Math.log n-trials)))]
                           [0.01 0.99]))))))
