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

import java.util.Locale;

import org.apache.lucene.search.similarities.Similarity;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.schema.SimilarityFactory;
import org.slf4j.Logger;

public class BoundedSimilarityFactory extends SimilarityFactory {
    private BoundedSimilaritySettings settings;
    private static Logger logger = Logs.logger;

    @Override
    public void init(SolrParams params) {
      super.init(params);
      settings = new BoundedSimilaritySettings(params);
      logger.info("Loaded BoundedSimilarityFactory. Settings={}", settings);
    }

    @Override
    public Similarity getSimilarity() {
        return new BoundedSimilarity(settings);
    }

    public static class BoundedSimilaritySettings {
        public final float maxIdf;
        public final float maxTf;
        public final float biasTf;
        public final int  forceTf;
        public final boolean  discountOverlaps;

        public BoundedSimilaritySettings(SolrParams params) {
            maxIdf = readFraction (params, "max_idf", 0.0f);
            maxTf = readFraction (params, "max_tf", 0.2f);
            int forceTf = params.getInt("force_tf", -1);
            this.forceTf = forceTf < 255 ? forceTf : 255;
            biasTf = params.getFloat("bias_tf", 0.6f);
            discountOverlaps = params.getBool("discount_overlaps", true);
        }
        private static float readFraction (SolrParams params, String key, float def) {
            float ret = params.getFloat(key, def);
            if (ret < 0 || ret > 1) 
                throw new RuntimeException (String.format("Invalid value for %s: [%s]: should be 0<=value<=1", key, ret));
            return ret;
        }
        
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "BoundedSimilarity[max_idf=%f, max_tf=%f, force_tf=%d, bias_tf=%f, discount_overlaps=%s]",
                    maxIdf,
                    maxTf,
                    forceTf,
                    biasTf,
                    discountOverlaps);
            
        }
    }
}
