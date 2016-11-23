(ns irresponsible.domiscuity.util
  (:require [clojure.string :as str])
  #?(:clj (:import [java.util.regex Pattern])))

(defn name-kw
  "Creates a keyword from a tag name, doing the right thing with namespace
   args: [val] ; string
   returns: keyword"
  [val]
  (let [[a b :as ps] (str/split val #":")]
    (case (count ps)
      1 (keyword val)
      2 (keyword a b)
      (throw (ex-info "Invalid tag name (too many colons)" {:got val})))))

(defn pat-flags
  "Gets the flags for the given regex options
   Which flags are accepted depends upon our host platform
   args: [opts] ; map of option keywords to truth values, default false
     valid keys (clj):  :multi-line? :single-line? :case-insensitive? :comments? :literal?
                        :unicode-case? :unicode-character-class? :unix-lines? :canon-eq?
     valid keys (cljs): :multi-line? :single-line? :case-insensitive? :global? :unicode? :sticky?
   returns: flags suitable for constructing a regex with (platform specific)"
  #?(:clj  [{:keys [multi-line? single-line? case-insensitive? comments? literal?
                    unicode-case? unicode-character-class? unix-lines? canon-eq?]}]
     :cljs [{:keys [multi-line? single-line? case-insensitive? global? unicode? sticky?]}])
  #?(:clj (let [opts  [multi-line? single-line? case-insensitive? comments? literal?
                       unicode-case? unicode-character-class? unix-lines? canon-eq?]
                flags [Pattern/MULTILINE Pattern/DOTALL  Pattern/CASE_INSENSITIVE
                       Pattern/COMMENTS  Pattern/LITERAL Pattern/UNICODE_CASE
                       Pattern/UNICODE_CHARACTER_CLASS   Pattern/UNIX_LINES  Pattern/CANON_EQ]]
            (apply bit-or (map #(if % %2 0) opts flags)))
     :cljs
     (let [opts  [multi-line? single-line? case-insensitive? global? unicode? sticky?]
           flags ["m" "s" "i" "g" "u" "y"]]
       (apply str/join "" (map #(if % %2 "") opts flags)))))

(defn re-pat
  "Constructs a regex pattern from a pattern string and an options map
   args: [pat opts]
     opts: map of options, see `pat-flags` for description
   returns: regex (platform specific)"
  [^String pat opts]
  (let [^int flags (pat-flags opts)]
    (#?(:clj Pattern/compile :cljs js/RegExp.)
     pat opts)))

(defn regex? [r]
  (instance? #?(:clj Pattern :cljs js/RegExp) r))

(defn re-match
  ""
  [])
  
(defn quote-re
  ""
  [re]
  (str/replace re #"[-/\\^$*+?.()|\[\]{}]" #(str "\\\\" %)))

