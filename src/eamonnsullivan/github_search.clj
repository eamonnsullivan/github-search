(ns eamonnsullivan.github-search
  (:require [clojure.string :as string]
            [clj-http.client :as client]
            [clojure.data.json :as json]))

(def ^:dynamic *page-size* 25)
(def ^:dynamic *github-url* "https://api.github.com/graphql")

(defn request-opts
  [access-token]
  {:ssl? true :headers {"Authorization" (str "bearer " access-token)}})

(def repo-query "query($first:Int!, $after: String, $query: String!) {
  search(type:REPOSITORY, query:$query, first: $first, after: $after) {
    repositoryCount
    nodes {
      ... on Repository {
        name
        description
        url
        sshUrl
        updatedAt
        languages(first: 10) {
          nodes {
            name
          }
        }
      }
    }
    pageInfo {
      hasNextPage
      endCursor
    }
  }
}")

(defn http-post
  [url payload opts]
  (client/post url (merge {:content-type :json :body payload} opts)))

(defn get-query
  [org topics]
  (string/trim (str "org:" org " " (string/join " " (doall (map #(str "topic:" %) topics))))))

(defn fix-languages
  [result]
  (map (fn [repo] (merge repo {:languages (into [] (map #(str (% :name)) (-> repo :languages :nodes)))})) result))

(defn get-nodes
  [page]
  (fix-languages (-> page :data :search :nodes)))

(defn get-page-of-repos
  [access-token org topics page-size cursor]
  (let [variables {:first page-size :query (get-query org topics) :after cursor}
        payload (json/write-str {:query repo-query :variables variables})
        response (http-post *github-url* payload (request-opts access-token))]
    (json/read-str (response :body) :key-fn keyword)))

(defn get-all-pages
  [access-token org topics]
  (let [page (get-page-of-repos access-token org topics *page-size* nil)]
    (loop [page page
           result []]
      (let [pageInfo (-> page :data :search :pageInfo)
            has-next (pageInfo :hasNextPage)
            cursor (pageInfo :endCursor)
            result (concat result (get-nodes page))]
        (if-not has-next
          (into [] (concat result (get-nodes page)))
          (recur (get-page-of-repos access-token org topics *page-size* cursor)
                 (concat result (get-nodes page))))))))

(defn get-repos
  "Get information about repos in a given organisation, with the specified topics"
  [access-token org topics]
  (get-all-pages access-token org topics))
