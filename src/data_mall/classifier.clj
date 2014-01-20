(ns data-mall.classifier)

(import java.io.ByteArrayInputStream)
(import java.io.ByteArrayOutputStream)
(import java.io.ObjectOutputStream)
(import java.io.ObjectInputStream)
(import java.io.IOException)

(import edu.stanford.nlp.classify.Classifier)
(import edu.stanford.nlp.classify.ColumnDataClassifier)
(import edu.stanford.nlp.classify.LinearClassifier)
(import edu.stanford.nlp.ling.Datum)
(import edu.stanford.nlp.objectbank.ObjectBank)
(import edu.stanford.nlp.util.ErasureUtils)




(defn train-test
  "This helps train the alogorthm and makes an evaluation to the result."
  [prop-file train-file test-file]
  (let [prop (ColumnDataClassifier. prop-file)
        train (.readTrainingExamples prop train-file)
        clfy (.makeClassifier prop train)]
    (for [line (ObjectBank/getLineIterator test-file)]
      (let [d (.makeDatumFromLine prop line 0)]
        (println (str (first line) " " (.classOf clfy d)))))))

#_(train-test
 "D:/algorithm/stanford-classifier-2014-01-04/examples/cheese2007.prop"
 "D:/algorithm/stanford-classifier-2014-01-04/examples/cheeseDisease.train"
 "D:/algorithm/stanford-classifier-2014-01-04/examples/cheeseDisease.train"
 )

(train-test
 "D:/training/dfl_douban_attitude.prop"
 "D:/training/dfl_douban_attitude.train"
 "D:/training/dfl_douban_attitude.test"
 )







;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;not that smart;;;;;;;;;;;;;;;;;;;;;

(def prop (ColumnDataClassifier. "D:/algorithm/stanford-classifier-2014-01-04/examples/cheese2007.prop"))

(def trainingdata (.readTrainingExamples prop "D:/algorithm/stanford-classifier-2014-01-04/examples/cheeseDisease.train"))

(def clfy (.makeClassifier prop trainingdata))

(for [line (ObjectBank/getLineIterator "D:/algorithm/stanford-classifier-2014-01-04/examples/cheeseDisease.test")]
  (let [d (.makeDatumFromLine prop line 0)]
    (println (str line " ==> " (.classOf clfy d)))))

(vec (.getDeclaredMethods (class prop)))


