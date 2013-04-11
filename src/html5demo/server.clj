(ns html5demo.server
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :as route]
            [compojure.handler :as handler]
            [aleph.http :as aleph]
            [lamina.core :as lamina]))
(def counter (atom 0))
(def eventsource-ch (atom (lamina/permanent-channel)))
(def websocket-ch (atom (lamina/permanent-channel)))
(def longpoll-ch (atom (promise)))

(defn msg-handler [msg]
  (if (= msg "reset") (reset! counter 0)))

(defn ws-handler [ws handshake]
  (reset! websocket-ch (lamina/permanent-channel))
  (lamina/receive-all ws msg-handler)
  (lamina/siphon @websocket-ch ws))

(defn send-event []
  (let [msg "Count is odd"]
    (lamina/enqueue @eventsource-ch (str "data: " msg "\n\n"))
    (lamina/enqueue @websocket-ch msg)
    (deliver @longpoll-ch msg)))

(defn increment []
  (let [ct (swap! counter inc)]
    (if (odd? ct)
      (send-event))
    (str ct)))

(defroutes main-handler
  (GET "/increment" [] (increment))
  (GET "/long-poll" [] (deref (reset! longpoll-ch (promise))))
  (GET "/socket" [] (aleph/wrap-aleph-handler ws-handler))
  (GET "/event-source" []
       {:status 200
        :headers {"Content-Type" "text/event-stream"}
        :body (reset! eventsource-ch (lamina/permanent-channel))})
  (route/resources "/")
  (route/not-found "Not Found"))

; Add aleph handler and start server
(defn -main []
  (let [port 8080]
    (println "server started on port" port)
    (aleph/start-http-server 
     (aleph/wrap-ring-handler main-handler) {:port port :websocket true})))

(def stop (-main))