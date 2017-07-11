; vim: syntax=clojure
(set-env! :dependencies (cond
  (= "1.8.0" (System/getenv "BOOT_CLOJURE_VERSION"))
    '[[org.clojure/clojure "1.8.0" :scope "provided"]
      [clojure-future-spec "1.9.0-alpha17"]]
  :else
    '[[org.clojure/clojure "1.9.0-alpha17" :scope "provided"]]))

(set-env!
  :project 'irresponsible/domiscuity
  :version "0.2.0"
  :resource-paths #{"src" "resources"}
  :source-paths #{"src"}
  :dependencies #(into % '[[org.jsoup/jsoup "1.10.3"]
                  [org.clojure/clojurescript   "1.9.671"        :scope "test"]
                  [adzerk/boot-test            "1.2.0"          :scope "test"]
                  [adzerk/boot-cljs            "2.0.0"          :scope "test"]
                  [crisptrutski/boot-cljs-test "0.3.2-SNAPSHOT" :scope "test"]]))

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-test :as t]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)
       :description "HTML Parsing and Dom manipulation for clj/cljs"
       :url "https://github.com/irresponsible/domiscuity"
       :scm {:url "https://github.com/irresponsible/domiscuity"}
       :license {"MIT" "https://en.wikipedia.org/MIT_License"}}
  push {:tag true
        :ensure-branch "master"
        :ensure-release true
        :ensure-clean true
        :gpg-sign true
        :repo "clojars"}
  target  {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths   #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask clj-test []
  (comp (testing) (t/test)))

(deftask cljs-test []
  (comp (testing) (test-cljs)))

(deftask test []
  (println "* Warning: test-cljs disabled for now")
  (comp (testing) (t/test)))

(deftask autotest-clj []
  (comp (testing) (watch) (speak) (t/test)))

(deftask autotest-cljs []
  (comp (testing) (watch) (speak) (test-cljs)))

(deftask autotest []
  (println "* Warning: test-cljs disabled for now")
  (comp (watch) (test)))

(deftask release []
  (comp (pom) (jar) (push)))

