(ns irresponsible.domiscuity.util-test
  (:require [irresponsible.domiscuity.util :as util]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest test-clojure-vec
  (let [array-like #?(:clj
                      (let [arr (make-array Integer/TYPE 3)]
                        (aset arr 0 1)
                        (aset arr 1 2)
                        (aset arr 2 3)
                        arr)
                      :cljs #js [1 2 3])]
    (t/testing "converts list-like items in to clojure vectors"
      (t/is (= [1 2 3] (util/clojure-vec array-like))))

    (t/testing "accepts an optional transducer"
      (t/is (= [1 3] (util/clojure-vec array-like (filter odd?)))))))

(t/deftest test-name-kw
  (t/is (= :div (util/name-kw "div")))
  (t/is (= :a/b (util/name-kw "a:b")))
  (t/is (nil? (util/name-kw "")))
  (t/is (nil? (util/name-kw nil)))
  (t/is (thrown? Exception (util/name-kw "a:b:c"))))
