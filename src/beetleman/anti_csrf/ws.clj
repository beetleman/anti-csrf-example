(ns beetleman.anti-csrf.ws
  (:require [ring.websocket :as ws]
            [mount.core :refer [defstate]]
            [beetleman.anti-csrf.cookie :as cookie]
            [ring.util.response :as resp]))

(defstate sockets
  :start (atom #{})
  :stop (doseq [s @sockets]
          (ws/close s)))

(defn on-open [ch]
  (tap> {::on-open [ch]})
  (swap! sockets conj ch))

(defn on-close [ch status-code reason]
  (tap> {::on-close [ch status-code reason]})
  (swap! sockets disj ch))

(defn on-message [current-ch message]
  (tap> {::on-message [current-ch message]})
  (doseq [ch @sockets]
    (when (not= ch current-ch)
      (ws/send ch (str "Broadcasting: " message)))))

(defn handler [r]
  (tap> {::request r})
  (cond
    (not (cookie/has-anti-csrf? r))
    (resp/bad-request "BAD HACKER")

    (:websocket? r)
    {::ws/listener
     {:on-open    #'on-open
      :on-message #'on-message
      :on-close   #'on-close}}

    :else
    (resp/not-found "Connect WebSockets to this URL")))
