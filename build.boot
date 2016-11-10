(set-env!
  :project 'irresponsible/domiscuity
  :version "0.1.0"
  :resource-paths #{"src"}
  :source-paths #{"src"}
  :dependencies '[[org.clojure/clojure    "1.9.0-alpha14" :scope "provided"]
                  [org.jsoup/jsoup "1.10.1"]
                  [irresponsible/gadget "0.2.0"                 :scope "test"]
                  [org.clojure/clojurescript   "1.9.293"        :scope "test"]
                  [adzerk/boot-test            "1.1.2"          :scope "test"]
                  [adzerk/boot-cljs            "1.7.228-2"      :scope "test"]
                  [ajchemist/boot-figwheel "0.5.4-6"            :scope "test"]
                  [org.clojure/tools.nrepl "0.2.12"             :scope "test"]
                  [com.cemerick/piggieback "0.2.1"              :scope "test"]
                  [figwheel-sidecar "0.5.7"                     :scope "test"]
                  [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT" :scope "test"]])

(require '[adzerk.boot-cljs :refer [cljs]]
         '[adzerk.boot-test :as t]
         '[crisptrutski.boot-cljs-test :refer [test-cljs]]
         '[boot-figwheel :refer [figwheel cljs-repl] :as fig])

(task-options!
  pom {:project (get-env :project)
       :version (get-env :version)
       :description "HTML Parsing and Dom manipulation for clj/cljs"
       :url "https://github.com/irresponsible/domiscuity"
       :scm {:url "https://github.com/irresponsible/domiscuity"}
       :license {"MIT" "https://en.wikipedia.org/MIT_License"}}
  figwheel '{:build-ids ["dev"]
             :all-builds [{:id "dev" :source-paths ["src"]
                           :compiler {:main qarma.client :output-to "app.js"}
                           :figwheel {:build-id "dev"
                                      :on-jsload qarma.client/main
                                      :heads-up-display true
                                      :autoload true
                                      :debug false}}]
             :figwheel-options {:open-file-command "emacsclient" :repl true}}
  repl  {:init-ns 'qarma.core}
  test-cljs {:js-env :phantom}
  target  {:dir #{"target"}})

(deftask testing []
  (set-env! :source-paths   #(conj % "test")
            :resource-paths #(conj % "test"))
  identity)

(deftask clj-tests []
  (comp (testing) (speak) (t/test)))

(deftask cljs-tests []
  (comp (testing) (speak) (test-cljs)))

(deftask tests []
  (comp (testing) (speak) (t/test) (test-cljs)))

(deftask autotest-clj []
  (comp (testing) (watch) (speak) (t/test)))

(deftask autotest-cljs []
  (comp (testing) (watch) (speak) (test-cljs)))

(deftask autotest []
  (comp (watch) (tests)))

;; (deftask dev-cljs []
;;   (comp (testing)
;;         (serve :dir "target/")
;;         (watch)
;;         (speak)
;;         (reload)
;;         (cljs-repl)
;;         (cljs :source-map true :optimizations :none)))

(deftask make-release-jar []
  (comp (pom) (jar)))

