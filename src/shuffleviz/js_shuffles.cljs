(ns shuffleviz.js-shuffles)

(def naive-shuffle-i->random
  (js/eval "(array) => {
           var array = [...array];
           var n = array.length, i = -1, j;
           while (++i < n) {
           j = Math.floor(Math.random() * n);
           t = array[j];
           array[j] = array[i];
           array[i] = t;
           }
           return array;
           }"))

(def naive-shuffle-random->random
  (js/eval "(array) => {
           var array = [...array];
           var n = array.length, i = -1, j, k;
           while (++i < n) {
           j = Math.floor(Math.random() * n);
           k = Math.floor(Math.random() * n);
           t = array[j];
           array[j] = array[k];
           array[k] = t;
           }
           return array;
           }"))

(def naive-shuffle-random-comparator
  (js/eval "(array) => {
           var array = [...array];
           array.sort(() => Math.random() - .5);
           return array;
           }"))

(def naive-shuffle-random-order
  (js/eval "(array) => {
           var array = [...array];
           var random = array.map(Math.random);
           array.sort(function(a, b) {
           return random[a] - random[b];
           });
           return array;
           }"
           ))

(def shuffle-fisher-yates
  (js/eval "(array) => {
           var array = [...array];
  var m = array.length, t, i;
  while (m) {
    i = Math.floor(Math.random() * m--);
    t = array[m];
    array[m] = array[i];
    array[i] = t;
  }
           return array;
}"))


