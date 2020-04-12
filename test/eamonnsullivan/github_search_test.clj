(ns eamonnsullivan.github-search-test
  (:require [clojure.test :refer :all]
            [eamonnsullivan.github-search :as sut]
            [clojure.data.json :as json]))

(def first-body (slurp "./test/eamonnsullivan/testresult.json"))
(def second-body (slurp "./test/eamonnsullivan/testresult2.json"))
(def post-response {:body first-body})

(deftest test-get-query
  (testing "handles multiple topics"
    (is (= "org:bbc topic:one topic:two topic:three" (sut/get-query "bbc" ["one" "two" "three"]))))
  (testing "handles no topics"
    (is (= "org:bbc" (sut/get-query "bbc" []))))
  (testing "handles topics as list"
    (is (= "org:bbc topic:one topic:two topic:three" (sut/get-query "bbc" '("one" "two" "three"))))))

(deftest test-fix-languages
  (let [page [{:languages {:nodes [{:name "Clojure"} {:name "Javascript"}]}}
              {:languages {:nodes [{:name "Scala"}]}}
              {:languages {:nodes [{:name "Scala"} {:name "HTML"}]}}]
        expected [{:languages ["Clojure" "Javascript"]}
                  {:languages ["Scala"]}
                  {:languages ["Scala" "HTML"]}]]
    (testing "collapses languages to a list"
      (is (= expected (sut/fix-languages page)))))
  (let [page [{:some-other-key "something"}]
        expected [{:some-other-key "something" :languages []}]]
    (testing "doesn't blow up if languages not found"
      (is (= expected (sut/fix-languages page))))))

(deftest test-get-nodes
  (let [page {:data {:search {:nodes [{:one 1} {:two 2}]}}}
        expected [{:one 1 :languages []} {:two 2 :languages []}]]
    (testing "gets nodes from results, adds languages"
      (is (= expected (sut/get-nodes page)))))
  (let [page {:data {:search {:nodes []}}}
        expected []]
    (testing "handles empty nodes"
      (is (= expected (sut/get-nodes page))))))

(deftest test-get-page-of-repos
  (with-redefs [sut/http-post (fn [_ _ _] post-response)]
    (testing "converts body to edn"
      (let [result (sut/get-page-of-repos "secret-token" "test" ["test"] 7 nil)]
        (is (= 7 (-> result :data :search :repositoryCount)))
        (is (= "project1" (-> result :data :search :nodes first :name)))
        (is (= "A description for project one" (-> result :data :search :nodes first :description)))
        (is (= "https://github.com/my-org/project1" (-> result :data :search :nodes first :url)))
        (is (= "project7" (-> result :data :search :nodes last :name)))))))

(deftest test-get-all-pages
  (with-redefs [sut/get-page-of-repos (fn [_ _ _ _ cursor]
                                        (if cursor
                                          (json/read-str second-body :key-fn keyword)
                                          (json/read-str first-body :key-fn keyword)))]
    (testing "follows pages"
      (let [result (sut/get-all-pages "secret-token" "test" ["test"])]
         (is (= "project8" (-> result last :name)))
        (is (= 16 (count result)))))))
