/*
 * Copyright 2022, De Bitmanager
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.bitmanager.solr.similarities;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.similarities.Similarity.SimScorer;

import nl.bitmanager.solr.similarities.BoundedSimilarityFactory.BoundedSimilaritySettings;

public class BoundedScorer extends SimScorer {
    private final Explanation idfExplain;
    private final Explanation boostExplain;

    private final float weight_idf;
    private final float weight_boost;
    private final float weight;

    private final float maxTf;
    private final float biasTf;
    private final float forceTf;


    public BoundedScorer(BoundedSimilarity parent, Explanation idfExplain, float boost, float idf)
    {
        final BoundedSimilaritySettings settings = parent.settings;
        this.idfExplain = idfExplain;
        maxTf = settings.maxTf;
        biasTf = settings.biasTf;
        forceTf = settings.forceTf;
        weight_idf = idf;
        weight_boost = boost;
        weight = idf*boost;
        boostExplain =  (boost==1.0f) ? null : Explanation.match(boost, String.format (Locale.ROOT, "*= boost=%.3f", boost));
    }

    private float score_tf(float freq, long norm) {
        float tf = freq;
        if (forceTf > 0) tf = forceTf;
        else if (tf > 255.0f) tf = 255.0f;

        float fnorm = (float)norm;
        return 1f + ((tf>=fnorm) ? maxTf : (float)(maxTf * Math.log(biasTf+tf) / Math.log(biasTf + fnorm)));
    }

    @Override
    public float score(float freq, long norm) {
        return weight * score_tf(freq, norm);
    }


    @Override
    public Explanation explain(Explanation freq, long norm) {
        float tfscore = score_tf((float)freq.getValue(), norm);
        float score = weight*tfscore;

        String msg = String.format (Locale.ROOT, "tf (cnt=%.1f, fieldlen=%d, maxTf=%.2f, forceTf=%.1f, bias=%.2f)", freq.getValue(), norm, maxTf, forceTf, biasTf);
        Explanation tfExplain = Explanation.match (tfscore, msg);
        
        if (idfExplain==null && boostExplain==null) {
            return tfExplain;
        }
        List<Explanation> sub = new ArrayList<Explanation>();
        sub.add (tfExplain);
        if (idfExplain != null) sub.add (idfExplain);
        if (boostExplain != null) sub.add (boostExplain);
        return Explanation.match (score, "Combined from:", sub);
    }



}
