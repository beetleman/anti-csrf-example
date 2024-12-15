(ns beetleman.anti-csrf.cookie
  (:require [ring.util.response :as resp]))

(def cookie-anti-csrf "cookie-anti-csrf")
(defonce cookie-anti-csrf-value (str (java.util.UUID/randomUUID)))

(defn set-anti-csrf-value [resp]
  (resp/set-cookie resp
                   cookie-anti-csrf
                   cookie-anti-csrf-value
                   {:same-site :strict
                    :http-only true}))

(defn has-anti-csrf? [{:keys [cookies]}]
  (= cookie-anti-csrf-value
     (get-in cookies [cookie-anti-csrf :value])))
