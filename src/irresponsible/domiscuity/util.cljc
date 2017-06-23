(ns irresponsible.domiscuity.util
  (:require [clojure.string :as str])
  #?(:clj (:import [java.util.regex Pattern])))

(defn clojure-seq [is]
  #?(:clj  (seq is)
     :cljs (array-seq is 0)))

(defn clojure-vec
  "Turns DOM collections into vectors, optionally transducing
   Note that this necessarily provides a static view
   args: [items] [items xform]
   returns: vector"
  ([is]
   (into [] (clojure-seq is)))
  ([is xform]
   (into [] xform (clojure-seq is))))

(defn name-kw
  "Creates a keyword from a tag name, doing the right thing with namespace
   args: [val] ; string
   returns: keyword"
  [val]
  (when (and val (seq val))
    (let [[a b :as ps] (str/split val #":")]
      (case (count ps)
        1 (keyword val)
        2 (keyword a b)
        (throw (ex-info "Invalid tag name (too many colons)" {:got val}))))))
