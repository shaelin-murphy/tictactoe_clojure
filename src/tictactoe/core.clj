(ns tictactoe.core
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.params :refer [wrap-params]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [hiccup.page :refer [html5]])
  (:gen-class))

;; Game state - track board and current player
(def game-state (atom {:board (vec (repeat 9 nil))
                       :current-player "X"
                       :winner nil}))

(defn check-winner [board]
  (let [winning-lines [[0 1 2] [3 4 5] [6 7 8]  ; rows
                       [0 3 6] [1 4 7] [2 5 8]  ; columns
                       [0 4 8] [2 4 6]]]        ; diagonals
    (some (fn [[a b c]]
            (when (and (get board a)
                       (= (get board a) (get board b) (get board c)))
              (get board a)))
          winning-lines)))

(defn check-draw [board]
  (and (every? some? board)
       (nil? (check-winner board))))

(defn place-move! [position]
  (swap! game-state
         (fn [{:keys [board current-player winner]}]
           (if (or winner (get board position))
             ;; Don't allow moves if there's a winner or cell is taken
             {:board board :current-player current-player :winner winner}
             ;; Make the move
             (let [new-board (assoc board position current-player)
                   new-winner (check-winner new-board)
                   next-player (if (= current-player "X") "O" "X")]
               {:board new-board
                :current-player next-player
                :winner new-winner})))))

(defn reset-game! []
  (reset! game-state {:board (vec (repeat 9 nil))
                      :current-player "X"
                      :winner nil}))

(defn render-cell [index value current-player winner]
  [:td {:style "border: 1px solid black; width: 100px; height: 100px; text-align: center; font-size: 48px;"}
   (if value
     value
     (when-not winner
       [:form {:method "post" :action "/move"}
        [:input {:type "hidden" :name "position" :value index}]
        [:input {:type "submit" :value current-player}]]))])

(defn game-page []
  (let [{:keys [board current-player winner]} @game-state
        draw? (check-draw board)]
    (html5
     [:head [:title "Tic Tac Toe"]]
     [:link {:rel "stylesheet" :href "/style.css"}]
     [:body
      [:h1 "Tic Tac Toe"]
      (cond
        winner [:h2 {:style "color: green;"} (str "Player " winner " wins!")]
        draw? [:h2 {:style "color: orange;"} "It's a draw!"]
        :else [:h2 (str "Current player: " current-player)])
      [:table {:style "border-collapse: collapse;"}
       [:tbody
        (for [row (partition 3 (map-indexed vector board))]
          [:tr
           (for [[idx val] row]
             (render-cell idx val current-player winner))])]]
      [:form {:method "post" :action "/reset"}
       [:input {:type "submit" :value "Reset"}]]])))

(defroutes app-routes
  (GET "/" [] (game-page))
  (POST "/move" [position]
    (place-move! (Integer/parseInt position))
    {:status 302 :headers {"Location" "/"}})
  (POST "/reset" []
    (reset-game!)
    {:status 302 :headers {"Location" "/"}})
  (route/resources "/")  ;; serve style.css
  (route/not-found "Page not found"))

(def app
  (wrap-params app-routes))

(defn -main [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "8080"))]
    (println (str "Starting server on port " port))
    (run-jetty app {:port port :join? false})))