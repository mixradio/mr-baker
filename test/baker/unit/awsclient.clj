(ns baker.unit.awsclient
  (:require [baker.awsclient :refer :all]
            [amazonica.aws
             [securitytoken :as sts]
             [ec2 :as ec2]]
            [environ.core :refer [env]]
            [midje.sweet :refer :all]))


(fact-group
 :unit

 (fact "alternative creds aren't merged if not required"
       (alternative-credentials-if-necessary "11111") => nil)

 (fact "alternative creds are added if required"
       (alternative-credentials-if-necessary "22222") => ..creds..
       (provided
        (sts/assume-role anything) => {:credentials ..creds..}))

 (fact "owned-amis-by-name returns the list of amis by date"
       (owned-amis-by-name ..name..) => '({:name "ent-exploud-0.9-1-2013-10-24_11-41-23-extra"}
                                            {:name "ent-exploud-0.10-1-2013-10-24_19-41-23"}
                                            {:name "ent-exploud-0.11-1-2013-10-25_18-41-23"})
       (provided
        (ec2/describe-images anything :owner ["self"] :filters anything) =>
        {:images [{:name "ent-exploud-0.10-1-2013-10-24_19-41-23"}
                  {:name "ent-exploud-0.9-1-2013-10-24_11-41-23-extra"}
                  {:name "ent-exploud-0.11-1-2013-10-25_18-41-23"}]}))

 (fact "owned-amis-by-name sorts where there are 3 version numbers"
       (owned-amis-by-name ..name..) => '({:name "ent-exploud-1.0.6-1-2012-11-24_18-41-23"}
                                            {:name "ent-exploud-1.0.99-1-2013-04-14_22-41-21"}
                                            {:name "ent-exploud-1.0.113-1-2013-10-24_18-41-35"}
                                            {:name "ent-exploud-1.1.0-1-2013-12-23_12-41-23"})
       (provided
        (ec2/describe-images anything :owner ["self"] :filters anything) =>
        {:images [{:name "ent-exploud-1.0.113-1-2013-10-24_18-41-35"}
                  {:name "ent-exploud-1.0.6-1-2012-11-24_18-41-23"}
                  {:name "ent-exploud-1.1.0-1-2013-12-23_12-41-23"}
                  {:name "ent-exploud-1.0.99-1-2013-04-14_22-41-21"}]}))

 (fact "owned-amis-by-name sorts basic ami names alphabetically"
       (owned-amis-by-name ..name..) => '({:name "entertainment-base-2012-10-24_00-00-03"}
                                            {:name "entertainment-base-2013-10-24_00-00-03"}
                                            {:name "entertainment-base-2013-10-25_00-00-03"})
       (provided
        (ec2/describe-images anything :owner ["self"] :filters anything) =>
        {:images [{:name "entertainment-base-2013-10-24_00-00-03"}
                  {:name "entertainment-base-2012-10-24_00-00-03"}
                  {:name "entertainment-base-2013-10-25_00-00-03"}]}))

 (fact "filter-active-amis removes actives amis"
       (filter-active-amis #{:active :inactive}) => #{:inactive}
       (provided
        (all-active-amis) => #{:active}))

 (fact "all-active-amis returns active amis from all environments"
       (all-active-amis) => #{:active-1 :active-2 :active-3}
       (provided
        (active-amis "11111") => #{:active-1}
        (active-amis "22222") => #{:active-2}
        (active-amis "33333") => #{:active-3})))
