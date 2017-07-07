(ns irresponsible.domiscuity.dom
  (:require [clojure.string :as str]
            [irresponsible.domiscuity.util :as u :refer [clojure-vec]])
  #?(:clj (:import [org.jsoup.parser Tag]
                   [org.jsoup.select Elements]
                   [org.jsoup.nodes Attribute Comment DataNode
                                    Document  Element Node TextNode])))

#?
(:clj
 (def ^:dynamic ^String *base-url*
   "The base url that is assigned to the dom nodes. Because jsoup demands something"
   "http://example.org/"))

(defn elem?
  "true if e is an element
   args: [e]
   returns: bool"
  [e]
  (instance? #?(:clj Element :cljs js/Element) e))

(defn attr?
  "true if a is an attribute
   args: [a]
   returns: bool"
  [a]
  (instance? #?(:clj Attribute :cljs js/Attr) a))

(defn text?
  "true if t is an text node
   args: [t]
   returns: bool"
  [t]
  (instance? #?(:clj TextNode  :cljs js/CharacterData) t))

(defn comment?
  "true if c is a comment
   args: [c]
   returns: bool"
  [c]
  (instance? #?(:clj Comment :cljs js/Comment) c))

#?
(:clj
 (defn jsoup-elements ^Elements [& elems]
   (let [es (Elements. ^int (count elems))]
     (doseq [^Element e elems]
       (.add es e))
     es)))

(defn -attr-name
  "Returns the name of an attribute as a keyword
   args: [attr]
   returns: keyword"
  [^Attribute a]
  (-> a #?(:clj .getKey :cljs .-name) u/name-kw))

(defn -attr-val
  "Returns the value of an attribute
   args: [attr]
   returns: string or true (for a boolean attribute)"
  [^Attribute a]
  (let [r (#?(:clj .getValue :cljs .-value) a)]
    (or (= "" r) r)))

(defn -attributes
  "Returns a vector of a node's -attributes, optionally transformed
   args: [node] [node xform]
   returns: vector"
  ([^Node n]
   (-attributes n identity))
  ([^Node n xform]
   (-> n #?(:clj  .attributes :cljs .-attributes) (clojure-vec xform))))

(defn -make-attr-name
  "Flexibly converts an attribute name so it is usable on an element.
   If it is a string, it is used as-is, for a keyword, we turn
   it into a string, detecting a namespace and using : to join if required
   args: [thing] ; a keyword? else stringified
   returns: string"
  ^String [n]
  (if (keyword? n)
    (if-let [ns (namespace n)]
      (str ns ":" (name n))
      (name n))
    (str n)))

(defn tag-name
  "Gets the tag name for an element as a keyword
   args: [elem]
   returns: keyword"
  [^Element e]
  (-> e #?(:clj .tagName :cljs .-nodeName) u/name-kw))

(defprotocol Text
  (text [this]
    "Returns the text contained within a text node as a string.
    args: [elem]
    returns: string")
  (set-text! [this ^String s]
    "Sets the text within a text node the given string.
     args: [elem string]
     returns: nil"))

#?(:clj
   (extend-protocol Text
     TextNode
     (text [t] (.text t))
     (set-text! [t ^String s]
       (.text t s))))

#?(:clj
   (extend-protocol Text
     Element
     (text [e]
       (when (.hasText e)
         (.text e)))
     (set-text! [e ^String s]
       (.text e s))))

#?(:clj
   (extend-protocol Text
     Comment
     (text [c] nil)
     (set-text! [e s]
       (throw (ex-info
               "Cannot set text of a comment" {:got e})))))

#?(:cljs
   (extend-protocol Text
     js/Element
     (text [e] (.-innerHtml e))
     (set-text! [e s]
       (throw (ex-info
               "Setting text is not supported in Clojurescript"
               {:got e})))))

;; TODO: pass transducer
(defn attrs
  "Returns a map of the element's attributes, key to value
   args: [node]
   return: "
  [elem]
  (into {} (map (fn [a] [(-attr-name a) (-attr-val a)]))
        (-attributes elem)))

(defn has-attr?
  "Returns true if the element contains the given attribute."
  [^Element elem attr]
  (#?(:clj .hasAttr) elem (-make-attr-name attr)))

(defn attr
  "Returns the value of an element's attribute by name
   args: [elem -attr-name]
   returns: string or true"
  [^Element elem attr]
  (if (has-attr? elem attr)
    (let [^String name (-make-attr-name attr)
          r (#?(:clj .attr :cljs .getAttribute) elem name)]
      (or (= "" r) r))
    nil))

(defn set-attr!
  "Sets an attribute on an element
   args [elem k v]
   returns: elem, mutated in place"
  ^Element [^Element e k v]
  #?(:clj (cond
            (instance? Boolean v) (.attr e (-make-attr-name k) ^boolean v)
            (nil? v) e
            :else (.attr e (-make-attr-name k) ^String (str v)))
     :cljs (.setAttribute e k v)))

(defn update-attr!
  "Like swap! but for attributes
   args: [element key fun]
   returns: elem, mutated in place"
  [e k f]
  (->> (attr e k)
       f
       (set-attr! e k)))

(defn set-attrs!
  "Sets attributes on an element from a map
   args: [elem kvs]
   returns: elem"
  [e kvs]
  (doseq [[k v] kvs]
    (set-attr! e k v))
  e)

(defn next-sibling
  "Returns the element following the provided one
   args: [elem]
   returns: element"
  [^Element elem]
  (#?(:clj .nextElementSibling :cljs .-nextElementSibling)
   elem))

(defn prev-sibling
  "Returns the element preceding the provided one
   args: [elem]
   returns: element"
  [^Element elem]
  (#?(:clj .previousElementSibling :cljs .-previousElementSibling)
   elem))

(defn children
  "Returns a sequence of a node's children, optionally transducing
   args: [node] [node xform]
   returns: seq of Node"
  ([^Node n]
   (children n identity))
  ([^Node n xform]
   (-> n #?(:clj .childNodes :cljs .-childNodes) (clojure-vec xform))))

(defn child-elems
  "Returns only the children who are elements (i.e. not text, comments...)
   args: [node] [node xform]
   returns: "
  ([^Node n]
   (child-elems n identity))
  ([^Node n xform]
   (-> n #?(:clj .children   :cljs .-children) (clojure-vec xform))))

(defn append!
  "Appends the given children to the Element
   args: [elem & children]
   returns: elem"
  [^Element elem & cs]
  (doseq [^Node c cs]
    (.appendChild elem c))
  elem)

(defn insert-before!
  "Inserts an element before the given element
   args: [marker elem]
     marker: the element you want to place the element before
     elem:   the element to insert
   returns: the newly inserted element"
  [^Node marker ^Node elem]
  #?(:clj  (try
             (.before marker elem) elem
             (catch IllegalArgumentException e
               (ex-info
                "Cannot insert before element"
                {:marker marker :elem elem}
                e)))
     :cljs (.insertBefore (.parent marker) elem marker)))

(defn insert-after!
  "Inserts an element after the given element
   args: [marker elem]
     marker: the element you want to place the element after
     elem:   the element to insert
   returns: the newly inserted element"
  [^Node marker ^Node elem]
  #?(:clj  (try
             (.after marker elem) elem
             (catch IllegalArgumentException e
               (throw (ex-info
                       "Cannot insert after element"
                       {:marker marker :elem elem}
                       e))))
     :cljs (.insertAfter (.parent marker) elem (when marker (next-sibling marker)))))

(defn make-element
  "Manufactures an element
   args: [tag-name] [tag-name attrs] [tag-name attrs children]
     tag-name: string naming the tag
     attrs:    map of attributes
     children: seq of children to add (or nil)
   returns: elem"
  (^Element [^String tag-name]
   #?(:clj  (Element. (Tag/valueOf tag-name) *base-url*)
      :cljs (.createElement js/Document tag-name)))
  (^Element [tag-name attrs]
   (set-attrs! (make-element tag-name) attrs))
  (^Element [tag-name attrs children]
   (apply append! (make-element tag-name attrs) children)))

(defn make-comment
  "Creates a comment node
   args: [comment] ; string
   returns: comment node"
  [^String c]
  #?(:clj  (Comment. c *base-url*)
     :cljs (js/Comment. c)))

(defn make-text
  "Creates a text node
   args: [text] ; string
   returns: text node"
  [^String t]
  #?(:clj  (TextNode. t *base-url*)
     :cljs (.createTextNode js/Document t)))

(defn detach!
  "Detaches the elements from their parents
   args: [& elems]
   returns: vector of elems removed"
  [& elems]
  #?(:clj (try
            (doto (apply jsoup-elements elems) .remove)
            (into [] elems)
            (catch IllegalArgumentException e
              (throw (ex-info "Cannot detach a top-level element" {:got elems} e))))
     :cljs (into [] (map #(do (.remove %) %)) elems)))

(defn detach-children!
  "Detaches the children of an element and returns them
   args: [elem]
   returns: vec of element"
  [^Element elem]
  #?(:clj (let [cs (children elem)]
            (.empty elem)
            (into [] cs))
     :cljs (apply detach (children elem))))

(defn query-all
  "querySelectorAll by another name
   args: [elem sel]
     elem: an Element
     sel:  a CSS selector string"
  [^Element elem ^String sel]
  (#?(:clj  .select
      :cljs .querySelectorAll)
   elem sel))

(defn find-elems
  "Returns a vector of all elements (including the provided one)
   recursively contained within the provided element and optionally transduced
   Note: text nodes are not Element
   args: [elem]
   returns: seq of Element"
  ([^Element elem]
   (find-elems elem identity))
  ([^Element elem xform]
   (-> elem
       #?(:clj  .getAllElements
          :cljs (query-all "*"))
       (clojure-vec xform))))

;; FIXME: These expose Attribute to the caller
(defn find-attr
  "Finds attributes on the given element passing pred
   args: [elem pred]
     pred: function of attribute object -> truthy
   returns: vector of elem"
  [elem pred]
  (-attributes elem (filter pred)))

(defn find-where-attr
  "Finds deeply all elements where an attribute passes pred
   args: [elem pred]
     pred: function of attribute object -> truthy
   returns: vector of elements"
  [elem pred]
  (find-elems elem (filter (comp seq #(find-attr % pred)))))

(defn find-by-attr
  "Finds deeply all elements with the given attribute
   args: [elem attr] [elem attr val]
     attr: an attribute name, string or keyword
     val:  a value to test against
   returns: vector of element"
  ([^Element elem ^String attr]
   #?(:clj  (clojure-vec (.getElementsByAttribute elem attr))
      :cljs (find-by-attr elem attr nil :has)))
  ([^Element elem ^String attr value]
   #?(:clj  (clojure-vec (.getElementsByAttributeValue elem attr value))
      :cljs (find-where-attr elem attr #(= value %)))))

(defn find-by-id
  "Returns the element with the given id string
   args: [elem-or-doc id]
   returns: Element or nil"
  [^Element elem ^String id]
  #?(:clj  (.getElementById elem id)
     :cljs (try ; getElementById only works on js/Document
             (.getElementById elem id) 
             (catch js/Object e ;; fallback to something awful
               (prn :caught e)
               (query-all (str "#" id))))))

(defn find-by-tag
  "Given an element or document, returns all tags with the matching name
   args: [elem-or-doc tag-name] [elem-or-doc tag-name mode]
     tag-name: lowercase string naming the tag
     mode: one of :has := :not= :contains :contains-word :matches :starts :ends
   returns: seq of tags"
  ([^Element elem ^String tag]
   (find-by-tag elem tag identity))
  ([^Element elem ^String tag xform]
   (-> (#?(:clj .getElementsByTag :cljs .getElementsByTagName)
        elem tag)
       (clojure-vec xform))))

(defn find-by-class
  "Given an element or document, selects deeply all elements with the selected class
   args: [elem class]
     class: string name of class
   returns: sequence of element"
  ([^Element elem ^String c]
   (find-by-class elem c identity))
  ([^Element elem ^String c xform]
   (-> (#?(:clj .getElementsByClass :cljs .getElementsByClassName)
        elem c)
       (clojure-vec xform))))
