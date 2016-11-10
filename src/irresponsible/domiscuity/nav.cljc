(ns irresponsible.domiscuity.nav
  #?(:clj (:import [org.jsoup Node])))

(defn clojure-seq
  "Turns DOM collections into sequences
   args: [items]
   returns: seq"
  [is]
  #?(:clj  (seq is)
     :cljs (array-seq is 0)))

(defn attr-name [a]
  (#?(:clj .getKey :cljs .-name) a))

(defn attr-val [a]
  (#?(:clj .getValue :cljs .-value) a))

(defn clj-attr [a & [key-only?]]
  [(attr-name a)
   (if key-only? a (attr-val a))])

(defn attributes
  "Returns a sequence of a node's attributes"
  #?(:clj [^Node n] :cljs [n])
  (clojure-seq (#?(:clj .attributes :cljs .-attributes) v)))

(defn attrs
  "Returns a map of key to attribute (not string!)"
  #?(:clj [^Node n] :cljs [n])
  (into {} (keep #(clj-attr % true)) (attributes n)))

(defn attrs-clj
  "Returns a map of key to attribute value (string)"
  #?(:clj [^Node n] :cljs [n])
  (into {} (keep clj-attr) (attributes n)))

(defn children
  #?(:clj [^Node n] :cljs [n])
  (#?(:clj .childNodes :cljs .-childNodes) n))
