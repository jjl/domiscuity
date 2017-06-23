(ns irresponsible.domiscuity.parser
  (:require [irresponsible.domiscuity.convertor :as c]
            [irresponsible.domiscuity.util :as util])
  #?(:clj (:import [org.jsoup Jsoup])))

(defn doc
  "Given a html document string, returns the relevant native Document
   args: [html-string]
   returns: Document"
  [^String html]
   #?(:clj  (Jsoup/parse html)
      :cljs (.parseFromString (js/DOMParser.) html "text/html")))

(def doc-clj
  "Parses a html document string into clojure data
   args: [html-string]
   returns: map"
  (comp c/native->clojure doc))

(defn frag
  "Given a body html fragment string, returns seq of top-level elems
   args: [html-string]
   returns: seq of Element"
  [^String html]
  #?(:clj  (-> html Jsoup/parseBodyFragment .body .childNodes util/clojure-vec)
     :cljs (-> (doc html)
               (.getElementsByTagName "body")
               .childNodes
               util/clojure-vec)))

(defn frag-clj
  "Given a body html fragment string, returns seq of top-level elems
   args: [html-string]
   returns: vec of map"
  [f]
  (c/native-into [] (frag f)))

