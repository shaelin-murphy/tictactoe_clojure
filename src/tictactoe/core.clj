(ns tictactoe.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.page :refer [html5]])
  (:gen-class))

;; Game state - just track which cells have X
(def game-state (atom (vec (repeat 9 nil))))

(defn place-x! [position]
  (swap! game-state assoc position "X"))

(defn reset-game! []
  (reset! game-state (vec (repeat 9 nil))))

(defn render-cell [index value]
  [:td {:style "border: 1px solid black; width: 100px; height: 100px; text-align: center; font-size: 48px;"}
   (if value
     value
     [:form {:method "post" :action "/move"}
      [:input {:type "hidden" :name "position" :value index}]
      [:input {:type "submit" :value "X"}]])])

(defn game-page []
  (let [board @game-state]
    (html5
     [:head [:title "Tic Tac Toe"]]
     [:body
      [:h1 "Tic Tac Toe"]
      [:table {:style "border-collapse: collapse;"}
       [:tbody
        (for [row (partition 3 (map-indexed vector board))]
          [:tr
           (for [[idx val] row]
             (render-cell idx val))])]]
      [:form {:method "post" :action "/reset"}
       [:input {:type "submit" :value "Reset"}]]])))

(defroutes app-routes
  (GET "/" [] (game-page))
  (POST "/move" [position]
    (place-x! (Integer/parseInt position))
    {:status 302 :headers {"Location" "/"}})
  (POST "/reset" []
    (reset-game!)
    {:status 302 :headers {"Location" "/"}})
  (route/not-found "Page not found"))

(def app
  (wrap-params app-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (println (str "Starting server on port " port))
    (run-jetty app {:port port :join? false})))