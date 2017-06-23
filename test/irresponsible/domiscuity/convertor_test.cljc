(ns irresponsible.domiscuity.convertor-test
  (:require [irresponsible.domiscuity.convertor :as c]
            [irresponsible.domiscuity.dom :as dom]
            [irresponsible.domiscuity.parser :as parser]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true]))
  #?(:clj (:import [org.jsoup.nodes DataNode])))

(defn kind? [kind elem]
  (= kind (:kind (c/native->clojure elem))))

(t/deftest test-native->clojure
  (t/testing "attribute"
    (let [attr (first (dom/-attributes
                       (dom/make-element "div" {:id "test"})))]
      (t/testing "keywordised"
        (binding [c/*keywordise-attrs* true]
          (t/is (= [:id "test"] (c/native->clojure attr)))))
      (t/testing "not keywordised"
        (binding [c/*keywordise-attrs* false]
          (t/is (= ["id" "test"] (c/native->clojure attr)))))))

  (t/testing "comment"
    (t/is (kind? :comment (dom/make-comment "testing, 1, 2"))))

  #?(:clj
     (t/testing "data node"
       (let [node (DataNode. "some data" "http://example.com")
             {:keys [kind text]} (c/native->clojure node)]
         (t/is (= :text kind))
         (t/is (= "some data" text)))))

  (t/testing "document"
    (t/is (kind? :document (parser/doc "<html></html>"))))

  (t/testing "element"
    (let [elem (c/native->clojure (dom/make-element "div"))]
      (t/is (= :element (:kind elem)))
      (t/is (= :div (:tag-name elem))))

    (t/testing "with attributes"
      (let [elem (c/native->clojure
                  (dom/make-element "div" {:class "small"}))]
        (t/is (= {:class "small"} (:attrs elem)))))

    (t/testing "with children"
      (let [children (repeatedly 3 #(dom/make-element "li"))
            elem (c/native->clojure
                  (dom/make-element "ul" {} children))]
        (t/is (= (repeat 3 {:kind :element
                            :tag-name :li
                            :attrs {}
                            :children []})
                 (:children elem))))))

  (t/testing "text node"
    (t/is (kind? :text (dom/make-text "abcde")))

    (t/testing "remove empty text"
      (binding [c/*remove-empty-text* true]
        (t/is (= "abcde" (->> (dom/make-text "abcde")
                              c/native->clojure
                              :text)))
        (t/is (nil? (c/native->clojure (dom/make-text "   "))))
        (t/is (nil? (c/native->clojure (dom/make-text "")))) ))
    (t/testing "keep empty text"
      (binding [c/*remove-empty-text* false]
        (t/is (= "abcde" (->> (dom/make-text "abcde")
                              c/native->clojure
                              :text)))
        (t/is (= "" (:text (c/native->clojure (dom/make-text "")))))

        (t/testing "with trimming"
          (binding [c/*trim-text* true]
            (t/is (= " " (:text (c/native->clojure (dom/make-text "  ")))))))

        (t/testing "without trimming"
          (binding [c/*trim-text* false]
            (t/is (= "  " (:text (c/native->clojure (dom/make-text "  "))))))))))

  (t/testing "nil"
    (t/is (nil? (c/native->clojure nil)))))

(t/deftest test-clojure->native
  (t/testing "comment"
    (t/is (dom/comment? (c/clojure->native {:kind :comment
                                            :comment "A comment"}))))

  (t/testing "text"
    (t/is (dom/text? (c/clojure->native {:kind :text, :text "Some text"}))))

  (t/testing "invalid type"
    (t/is (thrown? Exception (c/clojure->native {:kind :element})))
    (t/is (thrown? Exception (c/clojure->native nil)))))

(t/deftest test-native-info
  (t/is (= [{:kind :text, :text "abcde"}
            {:kind :element, :tag-name :div, :attrs {}, :children []}
            {:kind :comment, :comment "a comment"}]
           (c/native-into [] [(dom/make-text "abcde")
                              (dom/make-element "div")
                              (dom/make-comment "a comment")]))))
