(ns irresponsible.domiscuity.parser
  (:require [irresponsible.domiscuity.convertor :as c]
            [irresponsible.domiscuity.nav :as nav])
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
  #?(:clj  (.getChildNodes (.body (Jsoup/parseBodyFragment html)))
     :cljs (-> (parse-document html)
               (.getElementsByTagName "body")
               .childNodes)))

(defn frag-clj
  "Given a body html fragment string, returns seq of top-level elems
   args: [html-string]
   returns: vec of map"
  [f]
  (c/native-into-clojure [] (frag f)))

