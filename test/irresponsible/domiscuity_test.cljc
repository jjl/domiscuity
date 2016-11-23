(ns irresponsible.domiscuity-test
  (:require [irresponsible.domiscuity.dom :as d]
            [irresponsible.domiscuity.convertor :as c]
            [irresponsible.domiscuity.parser :as p]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true]))
  #?(:clj (:import [org.jsoup.parser Tag]
                   [org.jsoup.select Elements]
                   [org.jsoup.nodes Attribute BooleanAttribute Comment DataNode
                                    Document  Element Node TextNode])))


(def test-html
  "<!doctype html>
   <html>
   <head>
   </head>
   <body>
     <p id='attrs' foo='bar' baz='quux' booly></p>
     <p id='nav'>
       <a class='all-mine'>Some text</a>
       <a><!--a comment--></a>
       <a><b class='all-mine'>Foo</b></a>
     </p>
   </body>
   </html>")

(def doc (p/doc test-html))

(t/deftest dom-attrs
  (let [p1  (d/find-by-id doc "attrs")
        attrs1 (d/attributes p1)
        attrs2 (d/attributes p1 (map d/attr-val))
        exp-as {:foo "bar" :baz "quux" :booly true :id "attrs"}]
    (t/is (d/elem? p1))
    (t/is (= :p (d/tag-name p1)))
    (t/is (every? d/attr? attrs1))
    (t/is (= 4 (count attrs1) (count attrs2)))
    (t/is (every? (some-fn boolean? string?) attrs2))
    (t/is (= exp-as (d/attrs p1)))
    (doseq [[k v] exp-as]
      (let [r (d/attr p1 k)]
        (t/testing [k v r]
          (t/is (= v r)))))
    (t/is (= p1 (d/set-attr! p1 :foo "foo")))
    (t/is (= "foo" (d/attr p1 :foo)))
    (t/is (= p1 (d/set-attr! p1 :booly2 true)))
    (t/is (true? (d/attr p1 :booly2)))
    (t/is (= p1 (d/set-attr! p1 :booly2 false)))
    (t/is (true? (d/attr p1 :booly2))) ;; yes, i know. Blame the W3C...
    (t/is (= p1 (d/update-attr! p1 :foo (fn [foo] (t/is (= "foo" foo)) "bar"))))
    (t/is (= "bar" (d/attr p1 :foo)))
    (t/is (= p1 (d/set-attrs! p1 {:bar "baz" :quux true})))
    (t/is (= "baz" (d/attr p1 :bar)))
    (t/is (true? (d/attr p1 :quux)))))

(t/deftest dom-navigation
  (let [p2  (d/find-by-id doc "nav")
        ps  (d/query-all doc "p")
        ps2 (d/find-by-tag doc "p")
        cs1 (d/child-elems p2)
        cs2 (d/children p2)
        cs3 (filter d/elem? cs2)
        [c1 c2 c3] cs1
        am1 (d/find-by-class doc "all-mine")
        am2 (d/find-by-class doc "all-mine" (filter #(= :b (d/tag-name %))))]
    (t/is (d/elem? p2))
    (t/is (= :p (d/tag-name p2)))
    (t/is (> (count cs2) (count cs1))) ; text nodes
    (t/is (every? d/elem? cs1))
    (t/is (every? #(= :a (d/tag-name %)) cs1))
    (t/is (= cs1 cs3))
    (t/is (and (d/elem? c1) (d/elem? c2)))
    (t/is (= :a (d/tag-name c1) (d/tag-name c2)))
    (t/is (= {:kind :text :text "Some text"} (c/native->clojure (first (d/children c1)))))
    (t/is (= {:kind :comment :comment "a comment"} (c/native->clojure (first (d/children c2)))))
    (t/is (identical? c2 (d/next-sibling c1)))
    (t/is (identical? c1 (d/prev-sibling c2)))
    (t/is (= 2 (count ps)))
    (t/is (= ps ps2))
    (t/is (= 2 (count am1)))
    (t/is (= 1 (count am2)))
    ))

(t/deftest dom-manipulation
  (let [p (d/make-element "p")
        a (d/make-element "a" {:foo "bar"})
        e (d/make-element "em")
        b (d/make-element "b" {} [e])
        i (d/make-element "i")]
    (t/is (= :p (d/tag-name p)))
    (t/is (= :a (d/tag-name a)))
    (t/is (= :b (d/tag-name b)))
    (t/is (= :i (d/tag-name i)))
    (t/is (= "bar" (d/attr a :foo)))
    (t/is (= [e] (d/child-elems b)))
    (d/append! p a)
    (t/is (= a (first (d/child-elems p))))
    (d/insert-before! a b)
    (t/is (= b (d/prev-sibling a)))
    (t/is (= i (d/insert-after! a i)))
    (t/is (= i (d/next-sibling a)))
    (t/is (= 3 (count (d/child-elems p))))
    (t/is (= [p b e a i] (d/find-elems p)))
    (t/is (= [a] (d/detach! a)))
    (t/is (= 2 (count (d/child-elems p))))
    (t/is (= [b i] (d/detach-children! p)))
    (t/is (= 0 (count (d/child-elems p))))
    (t/is (d/comment? (d/make-comment "foo")))
    (t/is (d/text? (d/make-text "foo")))
    (t/is (= [p] (d/find-elems p)))))
