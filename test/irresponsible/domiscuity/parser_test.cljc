(ns irresponsible.domiscuity.parser-test
  (:require [irresponsible.domiscuity.parser :as parser]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true]))
  #?(:clj (:import [org.jsoup.nodes Document Element])))

(def html
  "<html>
    <head>
     <title>Testing the parser</title>
    </head>
    <body>
      <h1>You know nothing John Snow</h1>
    </body>
   </html>")

(t/deftest test-doc
  (let [doc (parser/doc html)]
    (t/is (instance? #?(:clj Document :cljs js/Document) doc))
    (t/is (= "Testing the parser" (#?(:clj .title :cljs .-title) doc)))))

(t/deftest test-doc-clj
  (t/is (= (parser/doc-clj html)
           {:kind :document,
            :nodes
            [{:kind :element,
              :tag-name :html,
              :attrs {},
              :children
              [{:kind :element,
                :tag-name :head,
                :attrs {},
                :children
                [{:kind :element,
                  :tag-name :title,
                  :attrs {},
                  :children [{:kind :text, :text "Testing the parser"}]}]}
               {:kind :element,
                :tag-name :body,
                :attrs {},
                :children
                [{:kind :element,
                  :tag-name :h1,
                  :attrs {},
                  :children
                  [{:kind :text, :text "You know nothing John Snow"}]}]}]}]})))

(t/deftest test-frag
  (let [frag (parser/frag "<p>Something<strong>BOLD!</strong></p>")]
    (t/is (= 1 (count frag)))
    (t/is (instance? #?(:clj Element :cljs js/Element) (first frag)))))

(t/deftest test-frag-clj
  (let [html "<p class='small'>Something<strong>BOLD!</strong></p>"]
    (t/is (= (parser/frag-clj html)
             [{:kind :element,
               :tag-name :p,
               :attrs {:class "small"},
               :children
               [{:kind :text, :text "Something"}
                {:kind :element,
                 :tag-name :strong,
                 :attrs {},
                 :children [{:kind :text, :text "BOLD!"}]}]}]))))
