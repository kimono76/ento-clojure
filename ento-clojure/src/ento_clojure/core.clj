(ns ento-clojure.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json])
  (:gen-class))

;; (defroutes app-routes
;;   (GET "/" [] simple-body-page)
;;   (GET "/request" [] request-example)
;;   (route/not-found "Error, page not found!"))


; Simple Body Page
(defn simple-body-page [req] ;(3)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Ento API for Math,Statistics, and ML, powered by Clojure"})
;
; request-example
(defn request-example [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->
             (pp/pprint req)
             (str "Request Object: " req))})

; Hello-name handler
; http://localhost:3000/hello?name=John%20Doe
(defn hello-name [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->
             (pp/pprint req)
             (str "Welcome " (:name (:params req))))})

; my people-collection mutable collection vector
(def people-collection (atom []))
(def area-collection (atom []))


;Collection Helper functions to add a new person


(defn addperson [firstname surname]
  (swap! people-collection conj {:firstname (str/capitalize firstname)
                                 :surname (str/capitalize surname)}))
(defn circle-area [pi radius]
  (swap! area-collection conj {:radius (str radius)
                               :pi (str pi)}))

(defn area [radius]
  (str " area " (* 3.14 (Integer/parseInt radius) (Integer/parseInt radius))))

(area "2")



;;  (* 3.14 2)

; Example JSON objects


(addperson "Functional" "Human")
(addperson "Micky" "Mouse")

; Return List of People
(defn people-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (str (json/write-str @people-collection))})

; Helper to get the parameter specified by pname from :params object in req
(defn getparameter [req pname] (get (:params req) pname))

; Add a new person into the people-collection
(defn addperson-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body    (-> (let [p (partial getparameter req)]
                  (str (json/write-str (addperson (p :firstname) (p :surname))))))})


; http://localhost:3000/circle?radius=2


(defn circle-handler [req]
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    (->
             (pp/pprint req)
             (str "Given this circle with radius "  (area (:radius (:params req))))
            ;;  (str "Given this circle with radius " (:radius (:params req)))
             )})

; Our main routes
(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET "/request" [] request-example)
  (GET "/hello" [] hello-name)
  (GET "/people" [] people-handler)
  (GET "/people/add" [] addperson-handler)
  (GET "/circle" [] circle-handler)
  (route/not-found "Error, page not found!"))

; Our main entry function
(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    ; Run the server without ring defaults
    ;(server/run-server #'app-routes {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))

