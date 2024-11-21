(ns beetleman.anti-csrf
  (:require
   [beetleman.anti-csrf.core]
   [mount.core :as mount])
  (:gen-class))

(defn start! [_args]
  (mount/start))

(defn stop! []
  (mount/stop))

(comment
  (stop!)
  (start! nil))

(defn -main
  [& args]
  (println "Startup...")
  (start! args)
  (println "ready")
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn []
                               (println "Shutdown...")
                               (stop!)
                               (println "bye!")))))
