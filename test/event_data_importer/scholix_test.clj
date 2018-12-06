(ns event-data-importer.scholix-test
  (:require [clojure.test :refer :all]
            [event-data-importer.scholix :as scholix]))

(deftest link-provider->source-token
  (testing "Consistent source token should be returned."
    (is
      (=
        (scholix/link-provider->source-token "Elsevier")
        (scholix/link-provider->source-token "Elsevier")
        (scholix/link-provider->source-token "Elsevier"))))

  (testing "Case should be ignored."
      (is
        (=
          (scholix/link-provider->source-token "ELSEVIER")
          (scholix/link-provider->source-token "Elsevier")
          (scholix/link-provider->source-token "elsevier"))))

  (testing "Different source token returned for different inputs."
    (is
      (not=
        (scholix/link-provider->source-token "Elsevier")
        (scholix/link-provider->source-token "ABC")))))

(deftest normalize
  (testing "Random string unchanged"
    (is (= (scholix/normalize-identifier "blah") "blah"))
    (is (= (scholix/normalize-identifier "") ""))
    (is (= (scholix/normalize-identifier nil) nil)))

  (testing "Non-DOI URL unchanged"
      (is (= (scholix/normalize-identifier "https://example.com") "https://example.com"))
      (is (= (scholix/normalize-identifier "https://notadoi.com/10.5555/12345678") "https://notadoi.com/10.5555/12345678")))

  (testing "Random string unchanged"
    (is (= (scholix/normalize-identifier "http://www.allelefrequencies.net/population/AFND112579") "http://www.allelefrequencies.net/population/AFND112579"))
    (is (= (scholix/normalize-identifier "https://www.ebi.ac.uk/cgi-bin/dbfetch?db=EMBL&id=KC492708") "https://www.ebi.ac.uk/cgi-bin/dbfetch?db=EMBL&id=KC492708")))

  (testing "DOI resolvers are normalized."
    (is (= "https://doi.org/10.5555/12345678"
           (scholix/normalize-identifier "10.5555/12345678")
           (scholix/normalize-identifier "http://dx.doi.org/10.5555/12345678")
           (scholix/normalize-identifier "https://doi.org/10.5555/12345678")
           (scholix/normalize-identifier "http://doi.org/10.5555/12345678")))))
