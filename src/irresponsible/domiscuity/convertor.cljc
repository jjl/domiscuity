(ns irresponsible.domiscuity.convertor
  (:require [irresponsible.domiscuity.dom :as dom]
            [irresponsible.domiscuity.util :as util])
  #?(:clj (:import [org.jsoup.nodes Attribute Comment DataNode
                    Document  Element TextNode])))

(def ^:dynamic *remove-empty-text*
  "Whether to remove text nodes that are 100% whitespace"
  true)

(def ^:dynamic *keywordise-attrs*
  "Whether to keywordise attribute keys"
  true)

#?
(:clj
 (def ^:dynamic *trim-text*
   "Whether to remove excess whitespace from around text nodes"
   true))

(defmulti native->clojure
  "Turns a native dom object into a clojure representation
   args: [dom-thing]
   dispatch: type
   contract: return a clojure data"
  type)

(defn native-into
  "Given something we can call seq on, returns a vector
   of calling native->clojure on the contents
   args: [items]
   returns: vector"
  [base is]
  (into base (keep native->clojure) (util/clojure-seq is)))

(defmulti clojure->native
  "Turns a clojure map into a native dom element
   args: [m]
   dispatch: :kind
   contract: return a native dom element or nil if there is none to display
             it is okay to throw if data is invalid"
  :kind)

(defmethod native->clojure :default [_] nil)

(defmethod clojure->native :default [fail]
  (throw
   (if (map? fail)
    (ex-info "Invalid kind" {:got (:kind fail)})
    (ex-info "Invalid clojure data" {:got fail}))))

#?
(:clj
 (defmethod native->clojure Attribute
   [^Attribute a]
   [(cond-> (.getKey a) *keywordise-attrs* keyword) (.getValue a)])
 :cljs
 (defmethod native->clojure js/Attr
   [a]
   [(cond-> (.-name a) *keywordise-attrs* keyword) (.-value a)]))

#?
(:clj
 (defmethod native->clojure Comment
   [^Comment c]
   {:kind :comment :comment (.getData c)})
 :cljs
 (defmethod native->clojure js/Comment
   [c]
   {:kind :comment :comment (.-data c)}))

(defmethod clojure->native :comment
  [{:keys [comment]}]
  (dom/make-comment comment))

#?
(:clj
 (defmethod native->clojure DataNode
   [^DataNode d]
   {:kind :text :text (.getWholeData d)}))

(defmethod native->clojure
  #?@(:clj  [Document [^Document d]]
      :cljs [js/Document [d]])
  (let [children (native-into [] (dom/children d))]
     {:kind :document :nodes children}))

(defmethod native->clojure
  #?@(:clj  [Element [^Element e]]
      :cljs [js/Element [e]])
   (let [tag-name (dom/tag-name e)
         attrs    (native-into {} (dom/-attributes e))
         children (native-into [] (dom/children e))]
     {:kind :element :tag-name tag-name
      :attrs attrs   :children children}))

#?
(:clj
  (defmethod native->clojure TextNode
    [^TextNode t]
    (when-not (and *remove-empty-text* (.isBlank t))
      {:kind :text :text (if *trim-text* (.text t) (.getWholeText t))}))
 :cljs
 (defmethod native->clojure js/Text ;; CDataSection (deprecated) isa Text
   [t]
   (when-not (and *remove-empty-text*
                  (.-isElementContentWhitespace t))
     {:kind :text :text (.-wholeText t)})))

(defmethod clojure->native :text
  [{:keys [text]}]
  (dom/make-text text))

