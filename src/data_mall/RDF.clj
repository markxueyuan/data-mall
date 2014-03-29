;the following code is problematic

(ns data-mall.RDF
  (:require [incanter.core :as incanter]
            [edu.ucdenver.ccp.kr.kb :as krkb]
            [edu.ucdenver.ccp.kr.rdf :as rdf]
            [edu.ucdenver.ccp.kr.sparql :as sparql]
            [edu.ucdenver.ccp.kr.sesame.kb :as sesamekb]
            [clojure.set :as set])
  (:import (java.io File)))

(defn kb-memstore
  "This creates a Sesame triple store of knowledge bases in memory"
  []
  (krkb/kb :sesame-mem))

(def tele-ont "http://telegraphis.net/ontology/")

(defn init-kb
  "This creates an in-memory knowledge base and initializes it with a default set of namespaces"
  [kb-store]
  (rdf/register-namespaces kb-store
                           '(
                             ("geographis" "http://telegraphis.net/ontology/geography/geography#")
                             ("code" "http://telegraphis.net/ontology/measurement/code#")
                             ("money" "http://telegraphis.net/ontology/money/money#")
                             ("owl" "http://www.w3.org/2002/07/owl#")
                             ("rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                             ("xsd" "http://www.w3.org/2001/XMLSchema#")
                             ("currency" "http://telegraphis.net/data/currencies/")
                             ("dbpedia" "http://dbpedia.org/resource/")
                             ("dbpedia-ont" "http://dbpedia.org/ontology/")
                             ("dbpedia-prop" "http://dbpedia.org/property/")
                             ("err" "http://ericrochester.com/"))))

(def tstore (init-kb (kb-memstore)))

(rdf/register-namespaces (kb-memstore)
                     '(("ex" "http://www.example.org/")
                       ("rdf" "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
                       ("foaf" "http://xmlns.com/foaf/0.1/")))

(def q '((?/c rdf/type money/Currency)
         (?/c money/name ?/full_name)
         (?/c money/shortName ?/name)
         (?/c money/symbol ?/symbol)
         (?/c money/minorName ?/minor_name)
         (?/c money/minorExponent ?/minor_exp)
         (?/c money/isoAlpha ?/iso)
         (?/c money/currencyOf ?/country)))

(defn header-keyword
  "This converts a query symbol to a keyword"
  [header-symbol]
  (keyword (.replace (name header-symbol))))

(defn fix-headers
  "This changes all the keys in the map to make them valid header keywords."
  [coll]
  (into {} (map (fn [[k v]] [(header-keyword k) v]) coll)))

(defn load-data
  [k rdf-file q]
  (rdf/load-rdf-file k rdf-file)
  (incanter/to-dataset (map fix-headers (sparql/query k q))))

(load-data tstore (File. "D:/data/currencies.ttl") q)




;;;;;;;;;;;;;;;;;;;tips;;;;;;;;;;;;;;;;;;;;


(name 'haha)




