# SolrSimilarityPlugin
Provides 2 custom similarity plugins for SOLR:

- Constant similarity
  To be used in places where you can't use a constant scorer. It simply assigns a constant score to a result.
- Bounded similarity
  A similarity that produces bounded, more predictable values. It is aimed to score small fields like names, keywords, categories, etc.
  It is possible to switch-off the tf-componentor the idf-component.



### Installation

SOLR nowadays supports plugins per index. Install by doing following steps:

- goto the solr_home of you index 
- create <solr_home>/lib directory
- copy the jar from the release into this directory
- stop / start SOLR.



### Configuration

Edit your managed-schema file and put following code in there:

```
<similarity class="nl.bitmanager.solr.similarities.BoundedSimilarityFactory">
      <float name="max_idf">0</float>
      <float name="max_tf">0.2</float>
      <float name="force_tf">-1</float>
      <float name="bias_tf">0.6</float>
      <bool name="discount_overlaps">true</bool>
    </similarity>
```

The above settings are the default, so you can omit them if you wish. 

The meaning of the parameters is:

| Parameter         | Default | Meaning                                                      |
| :---------------- | :------ | :----------------------------------------------------------- |
| max_idf           | 0       | Limits the influence of the idf.                             |
| max_tf            | 0.2     | Limits the influence of the tf.                              |
| force_tf          | -1      | Forces a tf (if >= 0).                                       |
| bias_tf           | 0.6     | Correction value in the division.                            |
| discount_overlaps | true    | Determines whether overlap tokens (Tokens with 0 position increment) are ignored when computing norm. |

The total score is calculated as  idf * tf, where 1<=idf<=(1+max_idf) and 0<=tf<=(1+max_tf).

As you can see, the idf component is switched off by default!



It is also possible to assign a similarity per fieldType. In that case, you have to put the above `<similarity>` node as a child of your `<fieldType>` node.
