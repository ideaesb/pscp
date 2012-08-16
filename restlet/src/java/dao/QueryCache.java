
package dao;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iws
 */
public class QueryCache {

    private static ReferenceQueue queue = new ReferenceQueue();
    private static List<Query> queries = new ArrayList<Query>();

    static synchronized void addQuery(Query q) {
        queries.add(q);
        new QueryRef(q);
    }

    static void clear() {
        for (Query q: queries) q.clear();
    }

    static void clean() {
        while (true) {
            Reference ref = queue.poll();
            if (ref == null) break;
            ref.clear();
        }
    }

    static class QueryRef extends SoftReference {
        private final Query query;

        public QueryRef(Query query) {
            super(new Object(), queue);
            this.query = query;
        }

        @Override
        public void clear() {
            super.clear();
            query.clear();
        }

    }
}
