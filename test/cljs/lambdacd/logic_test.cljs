(ns lambdacd.logic-test
  (:require
    [cljs.test :refer-macros [deftest is testing run-tests]]
    [lambdacd.testdata :refer [some-build-step with-name with-type with-output with-children with-step-id]]
    [lambdacd.testutils :refer [contains-value?]]
    [reagent.core :as r]
    [re-frame.core :as re-frame]
    [lambdacd.logic :as logic]))

(defn mock-fn []
  (let [received-args (atom [])]
    (with-meta (fn [& args]
                 (swap! received-args #(conj % (vec args))))
               {:received-args received-args})))

(defn received-args [mock]
  @(:received-args (meta mock)))

(defn has-received? [mock expected-arg]
  (let [received (received-args mock)
        result   (contains-value? expected-arg received)]
    (is result (str received " does not contain " expected-arg))))

(defn has-not-received? [mock expected-arg]
  (let [received (received-args mock)
        result   (contains-value? expected-arg received)]
    (is (not result) (str received " does contain " expected-arg))))

(def some-db {:displayed-build-number 42})

(deftest on-tick-test
  (testing "that a tick dispatches update-history"
    (with-redefs [re-frame/dispatch (mock-fn)]
                 (logic/on-tick some-db nil)
                 (is (has-received? re-frame/dispatch [[::logic/update-history]]))))
  (testing "that a tick dispatches update-pipeline-state"
    (with-redefs [re-frame/dispatch (mock-fn)]
                 (logic/on-tick some-db nil)
                 (has-received? re-frame/dispatch [[::logic/update-pipeline-state]])))
  (testing "that a tick doesn't dispatch update-pipeline-state if no build-number is set"
    (with-redefs [re-frame/dispatch (mock-fn)]
                 (logic/on-tick (assoc some-db :displayed-build-number nil) nil)
                 (has-not-received? re-frame/dispatch [[::logic/update-pipeline-state]]))))