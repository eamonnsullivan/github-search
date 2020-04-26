(ns eamonnsullivan.github-search
  (:require #?(:clj [clj-http.client :as client]
               :cljs [cljs-http.client :as http])
            #?(:cljs [cljs.core.async :refer [<!]])
            [clojure.string :as string]
            #?(:clj [clojure.data.json :as json])))

(def ^:dynamic *default-page-size* 25)
(def github-url "https://api.github.com/graphql")

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
        languages(first: 2 orderBy:{field: SIZE, direction:DESC}) {
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
  #?(:clj (client/post url (merge {:content-type :json :body payload} opts))
     :cljs (<! http/post url (merge {:content-type :json :body payload} opts))))

(defn get-query
  [org topics]
  (string/trim (str "org:" org " " (string/join " " (doall (map #(str "topic:" %) topics))))))

(defn fix-languages
  [result]
  (map (fn [repo] (merge repo {:languages (into [] (map #(str (% :name)) (-> repo :languages :nodes)))})) result))

(defn get-nodes
  [page]
  (fix-languages (-> page :data :search :nodes)))

#?(:clj defn get-payload
   [query variables]
   (json/write-str {:query query :variables variables}))

#?(:cljs defn get-payload
   [query variables]
   (clj->js {:query query :variables variables}))

(defn get-page-of-repos
  [access-token org topics page-size cursor]
  (let [variables {:first page-size :query (get-query org topics) :after cursor}
        payload (get-payload repo-query variables)
        response (http-post github-url payload (request-opts access-token))]
    #?(:clj (json/read-str (response :body) :key-fn keyword)
       :cljs (js->clj (.parse js/JSON (response :body)) :keywordize-keys true))))

(defn get-all-pages
  [access-token org topics page-size]
  (let [page (get-page-of-repos access-token org topics page-size nil)]
    (loop [page page
           result []]
      (let [pageInfo (-> page :data :search :pageInfo)
            has-next (pageInfo :hasNextPage)
            cursor (pageInfo :endCursor)
            result (concat result (get-nodes page))]
        (if-not has-next
          (into [] result)
          (recur (get-page-of-repos access-token org topics page-size cursor)
                 (get-nodes page)))))))

(defn get-repos
  "Get information about repos in a given organisation, with the specified topics"
  ([access-token org topics] (get-all-pages access-token org topics *default-page-size*))
  ([access-token org topics page-size] (get-all-pages access-token org topics page-size)))
