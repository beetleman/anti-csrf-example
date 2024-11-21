(ns beetleman.anti-csrf.core
  (:require [org.httpkit.server :as hk-server]
            [mount.core :as mount :refer [defstate]]
            [ring.util.response :as resp]
            [reitit.ring :as ring]
            [ring.middleware.cookies :refer [wrap-cookies]]
            [reitit.ring.middleware.exception :refer [exception-middleware]]
            [reitit.ring.middleware.parameters :refer [parameters-middleware]]
            [reitit.core :as r]
            [hiccup2.core :as h]))

(def cookie-anti-csrf "cookie-anti-csrf")
(defonce cookie-anti-csrf-value (str (java.util.UUID/randomUUID)))
(def server-port 9999)
(def hacker-server-port 8888)

(defn set-cookie-anti-csrf-value [resp]
  (resp/set-cookie resp
                   cookie-anti-csrf
                   cookie-anti-csrf-value
                   {:same-site :strict
                    :http-only true}))

(defn has-cookie-anti-csrf? [{:keys [cookies]}]
  (= cookie-anti-csrf-value
     (get-in cookies [cookie-anti-csrf :value])))

(defn url [{::r/keys [router]} name]
  (r/match->path (r/match-by-name router name)))

(defn html
  ([main & {:keys [title]
            :or {title "ANTI-CSRF"}}]
   (str (h/html
         (h/raw "<!DOCTYPE html>")
         [:html {:lang "en"}
          [:head
           [:meta {:name :viewport
                   :content "width=device-width, initial-scale=1"}]
           [:meta {:name :color-scheme
                   :content "light dark"}]
           [:link {:rel  :stylesheet
                   :href "https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css"}]
           [:title title]]
          [:body
           [:main.container
            main]]]))))

(defn page-index [r]
  (-> (html
       [:form {:action (url r :handler/delete)
               :method :post}
        [:input {:type :submit
                 :value "Delete"}]])
      (resp/response)
      (set-cookie-anti-csrf-value)))

(defn page-delete [r]
  (let [target (if (has-cookie-anti-csrf? r)
                 :handler/deleted
                 :handler/bad-hacker)]
    (resp/redirect (url r target) :see-other)))

(defn page-deleted [r]
  (-> (html
       [:div
        [:h1 "Deleted"]
        [:a {:href (url r :handler/index)}
         "back"]])
      (resp/response)))

(defn page-bad-hacker [r]
  (-> (html
       [:div
        [:h1 "Bad hacker!"]
        [:a {:href (url r :handler/index)}
         "back"]])
      (resp/response)))

(defn make-router []
  [["/" {:name :handler/index
         :get  page-index}]
   ["/delete" {:name :handler/delete
               :post page-delete}]
   ["/deleted" {:name :handler/deleted
                :get  page-deleted}]
   ["/bad-hacker" {:name :handler/bad-hacker
                   :get  page-bad-hacker}]])

(defn run-server [name port handler]
  (let [server (hk-server/run-server handler
                                     {:port port})]
    (println "Server" (str "`" name "`") "started on port" port)
    server))

(defstate server
  :start
  (run-server "app"
              server-port
              (-> (make-router)
                  ring/router
                  (ring/ring-handler (constantly (resp/not-found "Not found"))
                                     {:middleware [exception-middleware
                                                   parameters-middleware
                                                   wrap-cookies]})))
  :stop
  (server))

(defstate hacker-server
  :start
  (run-server "hacker"
              hacker-server-port
              (constantly (resp/response
                           (html
                            [:div
                             [:h1 "Hacker man 9000"]
                             [:form {:action (str "http://localhost:" server-port "/delete")
                                     :method :post}
                              [:input {:type :submit
                                       :value "Get free cookies!"}]]]
                            :title "Freee Cookies!!"))))
  :stop
  (hacker-server))

(comment
  (mount/start)
  (mount/stop))

(defn -main
  "Invoke me with clojure -M -m beetleman.anti-csrf"
  [& args]
  (mount/start))
