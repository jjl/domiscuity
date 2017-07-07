(ns irresponsible.domiscuity.dom-test
  (:require [irresponsible.domiscuity.dom :as dom]
            [irresponsible.domiscuity.parser :as parser]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true]))
  #?(:clj (:import [org.jsoup.nodes Attribute Comment Element TextNode])))

(t/deftest test-make-element
  (t/testing "create element using a tag name"
    (let [elem (dom/make-element "div")]
      (t/is (instance? #?(:clj Element :cljs js/Element) elem))
      (t/is (= "div" (#?(:clj .tagName :cljs .-nodeName) elem)))))

  (t/testing "supply a map of -attributes"
    (let [elem (dom/make-element "div" {:id "stark", :class "large"})
          get-attr #(#?(:clj .attr :cljs .getAttribute) %1 %2)]
      (t/is (= "stark" (get-attr elem "id")))
      (t/is (= "large" (get-attr elem "class")))))

  (t/testing "accepts a list of children"
    (let [elem (->> ["li" "li" "li"]
                    (map dom/make-element)
                    (dom/make-element "ul" {:id "my-things"}))
          children (#?(:clj .childNodes :cljs .-childNodes) elem)]
      (t/is (= 3 (count children)))
      (t/is (->> children
                 (map #(#?(:clj .tagName :cljs .-nodeName) %))
                 (every? #{"li"}))))))

(t/deftest test-make-comment
  (let [comment (dom/make-comment "King of the North")]
    (t/is (instance? #?(:clj Comment :cljs js/Comment) comment))
    (t/is (= "King of the North" (#?(:clj .getData :cljs .-data) comment)))))

(t/deftest test-make-text
  (let [text (dom/make-text "You know nothing")]
    (t/is (instance? #?(:clj TextNode :cljs js/TextNode) text))
    (t/is (= "You know nothing" (#?(:clj .text :cljs .-data) text)))))

(t/deftest test-elem?
  (t/is (dom/elem? (dom/make-element "video")))
  (t/is (not (dom/elem? (dom/make-comment "Winter is coming"))))
  (t/is (not (dom/elem? (dom/make-text "HODOR!"))))
  (t/is (not (dom/elem? nil))))

(t/deftest test-attr?
  (let [elem (dom/make-element "div" {:key "value"})
        attrs (#?(:clj .attributes :cljs .-attributes) elem)]
    (t/is (dom/attr? (first attrs))))
  (t/is (not (dom/attr? nil))))

(t/deftest test-text?
  (t/is (dom/text? (dom/make-text "HODOR!")))
  (t/is (not (dom/text? (dom/make-element "video"))))
  (t/is (not (dom/text? (dom/make-comment "Winter is coming"))))
  (t/is (not (dom/text? nil))))

(t/deftest test-comment?
  (t/is (dom/comment? (dom/make-comment "Winter is coming")))
  (t/is (not (dom/comment? (dom/make-element "video"))))
  (t/is (not (dom/comment? (dom/make-text "HODOR!"))))
  (t/is (not (dom/comment? nil))))

(t/deftest test-tag-name
  (t/is (= :div (dom/tag-name (dom/make-element "div")))))

(t/deftest test-text
  (t/is (= "HODOR!" (dom/text (dom/make-text "HODOR!"))))
  (t/is (thrown? Exception (dom/text (dom/make-comment "Winter is coming"))))
  (t/is (thrown? Exception (dom/text (dom/make-element "video")))))

(t/deftest test-set-text!
  (let [e (dom/make-text "HODOR!")]
    (dom/set-text! e "Hoorah!")
    (t/is (= "Hoorah!" (dom/text e))))
  (t/is (thrown? Exception
                 (dom/set-text! (dom/make-comment "abcde") "edcba")))
  (t/is (thrown? Exception
                 (dom/set-text! (dom/make-element "video") "not-video"))))

(t/deftest test-attributes
  (let [attr-map {:src "http://somesite.net/img.png"
                  :alt "A Lanister always pays his debts"}]
    (t/testing "returns a vector of a node's attributes"
      (let [elem (dom/make-element "img" attr-map)
            attrs (dom/-attributes elem)]
        (t/is (= 2 (count attrs)))
        (t/is (= (keys attr-map) (map dom/-attr-name attrs)))
        (t/is (= (vals attr-map) (map dom/-attr-val attrs)))))

    (t/testing "can provide an optional transducer"
      (let [xf (filter #(= :src (dom/-attr-name %)))
            elem (dom/make-element "div" attr-map)
            attrs (dom/-attributes elem xf)]
        (t/is (= 1 (count attrs)))
        (t/is (= :src (dom/-attr-name (first attrs)))))))

  (t/testing "returns an empty list when there are no attributes"
    (let [elem (dom/make-element "img")]
      (t/is (empty? (dom/-attributes elem))))))

(t/deftest test-make-attr-name
  (t/testing "returns plain strings as-is"
    (t/is (= "class" (dom/-make-attr-name "class"))))

  (t/testing "parses keywords"
    (t/testing "plain"
      (t/is (= "data-attr" (dom/-make-attr-name :data-attr))))

    (t/testing "namespaced"
      (t/is (= "rich:hickey" (dom/-make-attr-name :rich/hickey)))))

  (t/testing "coerces other types"
    (t/is (= "data-attr" (dom/-make-attr-name 'data-attr)))
    (t/is (= "" (dom/-make-attr-name nil)))))

(t/deftest test-attrs
  (let [attr-map {:src "http://somesite.net/img.png"
                  :alt "A Lanister always pays his debts"}]
    (t/testing "returns a vector of a node's attributes"
      (let [elem (dom/make-element "img" attr-map)
            attrs (dom/attrs elem)]
        (t/is (= 2 (count attrs)))
        (t/is (= (keys attr-map) (keys attrs)))
        (t/is (= (vals attr-map) (vals attrs)))))

    ;; TODO
    #_(t/testing "can provide an optional transducer"
      (let [xf (filter #(= :src (dom/-attr-name %)))
            elem (dom/make-element "div" attr-map)
            attrs (dom/attrs elem xf)]
        (t/is (= 1 (count attrs)))
        (t/is (= :src (first (keys attrs)))))))

  (t/testing "returns an empty map where there are no attributes"
    (let [elem (dom/make-element "div")]
      (t/is (empty? (dom/attrs elem))))))

(t/deftest test-has-attr?
  (let [elem (dom/make-element "div" {:a "a", :b "2", :c true, :d false, :e nil})]
    (t/is (dom/has-attr? elem :a))
    (t/is (dom/has-attr? elem :b))
    (t/is (dom/has-attr? elem :c))
    (t/is (not (dom/has-attr? elem :d)))
    (t/is (not (dom/has-attr? elem :e)))))

(t/deftest test-attr
  (let [attr-map {:src "http://somesite.net/img.png"
                  :alt "A Lanister always pays his debts"}
        elem (dom/make-element "img" attr-map)]
    (t/testing "returns the value of an attribute if found"
      (t/is (= (vals attr-map) (->> (keys attr-map)
                                    (map #(dom/attr elem %))))))

    (t/testing "returns nil if the attribute was not found"
      (t/is (nil? (dom/attr elem :not-there)))))

  (t/testing "returns nil if the attribute is present but not set to a value"
    (let [elem (dom/make-element "p" {:visible nil})]
      (t/is (= nil (dom/attr elem :visible)))))

  (t/testing "parses boolean values"
    (let [attr-map {:official-saying false
                    :repeated-frequently true}
          elem (dom/make-element "p" attr-map)]
      (t/testing "returns true if the attribute is true"
        (t/is (= true (dom/attr elem :repeated-frequently))))

      (t/testing "returns nil if the attribuete is false"
        (t/is (= nil (dom/attr elem :official-saying)))))))

(t/deftest test-set-attr!
  (t/testing "sets an existing attribute to something else"
    (let [elem (dom/make-element "div" {:class "small"})]
      (dom/set-attr! elem :class "large")
      (t/is (= "large" (dom/attr elem :class)))))

  (t/testing "creates a none existing attribute"
    (let [elem (dom/make-element "div")]
      (dom/set-attr! elem :class "large")
      (t/is (= "large" (dom/attr elem :class)))))

  (t/testing "converts boolean values"
    (let [elem (dom/make-element "div" {:visible false})]
      (dom/set-attr! elem :visible true)
      (t/is (= true (dom/attr elem :visible))))))

(t/deftest test-update-attr!
  (t/testing "updates attribute using a function"
    (let [elem (dom/make-element "div" {:food "chocolate", :bitter false})]
      (dom/update-attr! elem :food #(str "dark " %))
      (t/is (= "dark chocolate" (dom/attr elem :food)))
      (dom/update-attr! elem :bitter not)
      (t/is (= true (dom/attr elem :bitter)))))

  (t/testing "creates attribute if it does not exist"
    (let [elem (dom/make-element "div")]
      (dom/update-attr! elem :food (constantly "chocolate"))
      (t/is (= "chocolate" (dom/attr elem :food))))))

(t/deftest test-set-attrs!
  (let [elem (dom/make-element "div" {:class "small"})]
    (dom/set-attrs! elem {:class "large", :id "some-thing"})
    (t/is (= "large" (dom/attr elem :class)))
    (t/is (= "some-thing" (dom/attr elem :id)))))

(t/deftest test-find-attr
  (t/testing "returns a list of attributes matching a predicate"
    (let [elem  (dom/make-element "div" {:class "small", :id "some-thing"})
          attrs (dom/find-attr elem #(= \s (first (dom/-attr-val %))))]
      (t/is (= 2 (count attrs)))))

  (t/testing "returns an empty list when predicate does not match"
    (let [elem (dom/make-element "div" {:a "1", :b "2"})]
      (t/is (empty? (dom/find-attr elem (constantly false)))))))

(def test-html
  "<!doctype html>
   <html>
   <head>
   </head>
   <body>
     <p id='attrs' foo='bar' baz='quux' booly></p>
     <p id='attrs' baz='lies'> <!-- it lies! -->
     <p id='nav'>
       <a class='all-mine'>Some text</a>
       <a><!--a comment--></a>
       <a><b class='all-mine'>Foo</b></a>
     </p>
   </body>
   </html>")

(def doc)

(t/use-fixtures :each
  (fn [f]
    #?(:clj  (alter-var-root #'doc (fn [_] (parser/doc test-html)))
       :cljs (set! doc (parser/doc test-html)))
    (f)))

(t/deftest test-find-by-id
  (t/testing "selects only a single element with the given id"
    (let [elem (dom/find-by-id doc "nav")]
      (t/is (dom/elem? elem))
      (t/is (= "nav" (dom/attr elem "id")))))

  (t/testing "selects the first item when there are multiple matches"
    (let [elem (dom/find-by-id doc "attrs")]
      (t/is (dom/elem? elem))
      (t/is (= "bar" (dom/attr elem "foo")))))

  (t/testing "returns nil when no items are found"
    (let [elem (dom/find-by-id doc "not-there")]
      (t/is (nil? elem)))))

(t/deftest test-find-by-tag
  (t/testing "selects all tags with the matching name"
    (let [elems (dom/find-by-tag doc "a")]
      (t/is (= 3 (count elems)))
      (t/is (every? #{:a} (map dom/tag-name elems)))))

  (t/testing "can provide an optional transducer"
    (let [xf (filter #(.hasClass % "all-mine"))
          elems (dom/find-by-tag doc "a" xf)]
      (t/is (= 1 (count elems)))
      (t/is (->> elems
                 (map #(dom/attr % :class))
                 (every? #{"all-mine"})))))

  (t/testing "empty list when none are found"
    (t/is (empty? (dom/find-by-tag doc "video")))))

(t/deftest test-find-by-class
  (t/testing "deeply selects all elements with the selected class"
    (let [elems (dom/find-by-class doc "all-mine")]
      (t/is (= 2 (count elems)))
      (t/is [:a :b] (map dom/tag-name elems))
      (t/is (->> elems
                 (map #(dom/attr % :class))
                 (every? #{"all-mine"})))))

  (t/testing "can provide an optional transducer"
    (let [xf (filter #(= :b (dom/tag-name %)))
          elems (dom/find-by-class doc "all-mine" xf)]
      (t/is (= 1 (count elems)))
      (t/is [:a] (map dom/tag-name elems))))

  (t/testing "empty list when none are found"
    (t/is (empty? (dom/find-by-class doc "not-here")))))

;; FIXME: does not accept keywords for attr argument
(t/deftest test-find-by-attr
  (t/testing "selects all elements with the attribute"
    (let [elems (dom/find-by-attr doc "baz")]
      (t/is (= 2 (count elems)))
      (t/is (->> elems
                 (map dom/attrs)
                 (every? #(contains? % :baz))))))

  (t/testing "can select elements that match attribute name and value"
    (let [elems (dom/find-by-attr doc "baz" "quux")]
      (t/is (= 1 (count elems)))
      (t/is (= "quux" (:baz (dom/attrs (first elems)))))))

  (t/testing "returns an empty list when no elements are found"
    (t/is (empty? (dom/find-by-attr doc "quack")))
    (t/is (empty? (dom/find-by-attr doc "quack" "uh, what?")))))

;; TODO: Change API to not expose Attribute
(t/deftest test-find-where-attr
  (t/testing "selects elements that pass predicate"
    (let [elems (dom/find-where-attr doc #(= (dom/-attr-val %) "quux"))]
      (t/is (= 1 (count elems)))))

  (t/testing "returns an empty list if none are found"
    (t/is (empty? (dom/find-where-attr doc (constantly false))))))

(t/deftest test-next-sibling
  (t/testing "returns the next sibling of an element"
    (let [elem (dom/find-by-id doc "attrs")
          sibling (dom/next-sibling elem)]
      (t/is (= {:id "attrs", :baz "lies"} (dom/attrs sibling)))))

  (t/testing "returns nil when there is no sibling"
    (let [elem (first (dom/find-by-tag doc "b"))]
      (t/is (nil? (dom/next-sibling elem))))))

(t/deftest text-prev-sibling
  (t/testing "returns the previous sibling of an element"
    (let [elem (first (dom/find-by-attr doc "baz" "lies"))
          sibling (dom/prev-sibling elem)]
      (t/is (= {:id "attrs", :foo "bar", :baz "quux", :booly true}
               (dom/attrs sibling)))))

  (t/testing "returns nil when there is no sibling"
    (let [elem (first (dom/find-by-tag doc "b"))]
      (t/is (nil? (dom/prev-sibling elem))))))

(t/deftest test-children
  (t/testing "returns a list of children for an element"
    (let [elem (dom/find-by-id doc "nav")
          children (dom/children elem)]
      ;; empty spaces are counted
      (t/is (= 7 (count children)))))

  (t/testing "can provide an optional transducer"
    (let [elem (dom/find-by-id doc "nav")
          xf (filter dom/elem?)
          children (dom/children elem xf)]
      (t/is (= 3 (count children)))
      (t/is (every? #{:a} (map dom/tag-name children)))))

  (t/testing "returns an empty list if there are no children"
    (let [elem (first (dom/find-by-tag doc "b"))]
      (t/is (empty? (dom/children elem (filter dom/elem?)))))))

(t/deftest test-child-elems
  (t/testing "returns a list of child elements for an element"
    (let [elem (dom/find-by-id doc "nav")
          children (dom/child-elems elem)]
      (t/is (= 3 (count children)))
      (t/is (every? #{:a} (map dom/tag-name children)))))

  (t/testing "can provide an optional transducer"
    (let [elem (dom/find-by-id doc "nav")
          xf (filter #(= (dom/attr % :class) "all-mine"))
          children (dom/child-elems elem xf)]
      (t/is (= 1 (count children)))))

  (t/testing "returns an empty list if there are no child elements"
    (let [elem (first (dom/find-by-tag doc "b"))]
      (t/is (empty? (dom/child-elems elem))))))

(t/deftest test-append!
  (t/testing "appends children to an element"
    (let [elem (dom/make-element "ul")
          children (repeatedly 3 #(dom/make-element "li"))]
      (apply dom/append! elem children)
      (t/is (= children (dom/children elem)))))

  (t/testing "does nothing if you pass no children"
    (let [elem (dom/make-element "ul")]
      (dom/append! elem)
      (t/is (empty? (dom/children elem))))))

(t/deftest test-insert-before!
  (t/testing "inserts an element before the current one"
    (let [elem (dom/find-by-id doc "attrs")
          next-elem (dom/make-element "p")]
      (dom/insert-before! elem next-elem)
      (t/is (= next-elem (dom/prev-sibling elem)))))

  #_(t/testing "throws an exception if it is not a top level element"
    (let [elem (dom/make-element "p")
          prev-elem (dom/make-element "h1")]
      (t/is (thrown? Exception (dom/insert-before! elem prev-elem)))))

  (t/testing "does nothing when inserting nil"
    (let [elem (dom/make-element "p")]
      (dom/insert-before! elem nil)
      (t/is (nil? (dom/prev-sibling elem))))))

(t/deftest test-insert-after!
  (t/testing "inserts an element after the current one"
    (let [elem (dom/find-by-id doc "attrs")
          next-elem (dom/make-element "p")]
      (dom/insert-after! elem next-elem)
      (t/is (= next-elem (dom/next-sibling elem)))))

  (t/testing "throws an exception if it is a top level element"
    (let [elem (dom/make-element "h1")
          next-elem (dom/make-element "p")]
      (t/is (thrown? Exception (dom/insert-after! elem next-elem)))))

  (t/testing "throws an exception when using nil"
    (let [elem (dom/make-element "h1")]
      (t/is (thrown? Exception (dom/insert-after! elem nil))))))

(t/deftest test-detach!
  (t/testing "detaches elements from their parents"
    (let [children (repeatedly 3 #(dom/make-element "li"))
          elem (dom/make-element "ul" {} children)]
      (t/is (= children (apply dom/detach! children)))
      (t/is (empty? (dom/child-elems elem)))))

  (t/testing "throws an exception if the element doesn't have a parent"
    (let [elem (dom/make-element "ul")]
      (t/is (thrown? Exception (dom/detach! elem))))))

(t/deftest test-detach-children!
  (t/testing "deataches the children of an element"
    (let [children (repeatedly 3 #(dom/make-element "li"))
          elem (dom/make-element "ul" {} children)]
      (t/is (= children (dom/detach-children! elem)))
      (t/is (empty? (dom/child-elems elem)))))

  (t/testing "returns an empty list if element doesn't have children"
    (let [elem (dom/make-element "ul")]
      (t/is (empty? (dom/detach-children! elem))))))

(t/deftest test-query-all
  (let [elems (dom/query-all doc "#nav")]
    (t/is (= 1 (count elems)))
    (t/is (= "nav" (dom/attr (first elems) :id))))

  (let [elems (dom/query-all doc "#attrs[baz='lies']")]
    (t/is (= 1 (count elems)))
    (t/is (= {:id "attrs", :baz "lies"} (dom/attrs (first elems)))))

  (t/is (= 2 (count (dom/query-all doc "#attrs"))))
  (t/is (empty? (dom/query-all doc "#invalid.element")))
  (t/is (= 2 (count (dom/query-all doc ".all-mine")))))

(t/deftest test-find-elems
  (t/testing "recursively returns all child elements"
    (let [elems (dom/find-elems (dom/find-by-id doc "nav"))]
      (t/is (= 5 (count elems)))
      (t/is (every? #{:p :a :b} (map dom/tag-name elems)))))

  (t/testing "can provide an optional transducer"
    (let [xf (filter #(= :a (dom/tag-name %)))
          elems (dom/find-elems (dom/find-by-id doc "nav") xf)]
      (t/is (= 3 (count elems)))
      (t/is (every? #{:a} (map dom/tag-name elems)))))

  (t/testing "returns only the provided element if none exist"
    (let [elems (dom/find-elems (first (dom/find-by-tag doc "b")))]
      (t/is (= 1 (count elems)))
      (t/is (= :b (dom/tag-name (first elems)))))))
