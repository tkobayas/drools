/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.drools.core.util.index;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class IntervalIndexTest {

    @Test
    public void testNoOverlap() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();
        IntegerInterval a = new IntegerInterval("A", 0, true, 10, false); // age >= 0 && age < 10
        IntegerInterval b = new IntegerInterval("B", 10, true, 20, false); // age >= 10 && age < 20
        IntegerInterval c = new IntegerInterval("C", 20, true, 30, false); // age >= 20 && age < 30
        index.addInterval(a);
        index.addInterval(b);
        index.addInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(30)).isEmpty();
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, b, c);

        index.removeInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(25)).isEmpty();

        index.removeInterval(b);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).isEmpty();

        IntegerInterval d = new IntegerInterval("D", 10, true, 30, false); // age >= 10 && age < 30
        IntegerInterval e = new IntegerInterval("E", 30, true, 40, false); // age >= 30 && age < 40
        index.addInterval(d);
        index.addInterval(e);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, d, e);

        index.removeInterval(a);
        index.removeInterval(d);
        index.removeInterval(e);

        assertThat(index.getMapSize()).isEqualTo(0);
    }

    @Test
    public void testNoOverlap2() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();
        IntegerInterval a = new IntegerInterval("A", 0, false, 10, true); // age > 0 && age <= 10
        IntegerInterval b = new IntegerInterval("B", 10, false, 20, true); // age > 10 && age <= 20
        IntegerInterval c = new IntegerInterval("C", 20, false, 30, true); // age > 20 && age <= 30
        index.addInterval(a);
        index.addInterval(b);
        index.addInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).isEmpty();
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, b, c);

        index.removeInterval(c);

        assertThat(index.getIntervals(0)).isEmpty();
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(25)).isEmpty();
        assertThat(index.getIntervals(30)).isEmpty();

        index.removeInterval(b);

        assertThat(index.getIntervals(0)).isEmpty();
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(15)).isEmpty();

        IntegerInterval d = new IntegerInterval("D", 10, false, 30, true); // age > 10 && age <= 30
        IntegerInterval e = new IntegerInterval("E", 30, false, 40, true); // age > 30 && age <= 40
        index.addInterval(d);
        index.addInterval(e);

        assertThat(index.getIntervals(0)).isEmpty();
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(40)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(45)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, d, e);

        index.removeInterval(a);
        index.removeInterval(d);
        index.removeInterval(e);

        assertThat(index.getMapSize()).isEqualTo(0);
    }

    @Test
    public void testNoOverlapWithGap() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();
        IntegerInterval a = new IntegerInterval("A", 0, true, 10, false); // age >= 0 && age < 10
        IntegerInterval b = new IntegerInterval("B", 10, false, 20, false); // age > 10 && age < 20
        IntegerInterval c = new IntegerInterval("C", 30, true, 40, false); // age >= 30 && age < 40
        index.addInterval(a);
        index.addInterval(b);
        index.addInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).isEmpty();
        assertThat(index.getIntervals(11)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(25)).isEmpty();
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, b, c);

        index.removeInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).isEmpty();
        assertThat(index.getIntervals(11)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(25)).isEmpty();
        assertThat(index.getIntervals(30)).isEmpty();

        assertThat(index.getMapSize()).isEqualTo(4);

        index.removeInterval(b);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).isEmpty();
        assertThat(index.getIntervals(11)).isEmpty();

        IntegerInterval d = new IntegerInterval("D", 10, false, 30, true); // age > 10 && age <= 30
        index.addInterval(d);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).isEmpty();
        assertThat(index.getIntervals(11)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, d);

        index.removeInterval(a);
        index.removeInterval(d);

        assertThat(index.getMapSize()).isEqualTo(0);
    }

    @Test
    public void testOverlap() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();
        IntegerInterval a = new IntegerInterval("A", 0, true, 20, false); // age >= 0 && age < 20
        IntegerInterval b = new IntegerInterval("B", 10, true, 20, true); // age >= 10 && age <= 20
        IntegerInterval c = new IntegerInterval("C", 10, true, 30, false); // age >= 10 && age < 30
        index.addInterval(a);
        index.addInterval(b);
        index.addInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a, b, c);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(a, b, c);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(b, c);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(30)).isEmpty();
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, b, c);

        index.removeInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a, b);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(a, b);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(b);
        assertThat(index.getIntervals(25)).isEmpty();

        index.removeInterval(b);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(a);
        assertThat(index.getIntervals(20)).isEmpty();

        IntegerInterval d = new IntegerInterval("D", 25, false, 30, true); // age > 25 && age <= 30
        IntegerInterval e = new IntegerInterval("E", -10, false, 30, true); // age > -10 && age <= 30
        IntegerInterval f = new IntegerInterval("F", 15, true, 20, true); // age >= 15 && age <= 20
        index.addInterval(d);
        index.addInterval(e);
        index.addInterval(f);

        assertThat(index.getIntervals(-10)).isEmpty();
        assertThat(index.getIntervals(-1)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a, e);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(a, e);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a, e);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(a, e, f);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(e, f);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(d, e);
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, d, e, f);

        index.removeInterval(a);
        index.removeInterval(d);
        index.removeInterval(e);
        index.removeInterval(f);

        assertThat(index.getMapSize()).isEqualTo(0);
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidInterval() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();
        IntegerInterval a = new IntegerInterval("A", 20, true, 10, false); // age >= 20 && age < 10
        index.addInterval(a);
    }

    @Test(expected = IllegalStateException.class)
    public void testInvalidInterval2() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();
        IntegerInterval a = new IntegerInterval("A", 20, true, 20, false); // age >= 20 && age < 20
        index.addInterval(a);
    }

    @Test
    public void testPoint() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();

        // This is a point, not an interval. But valid anyway
        IntegerInterval a = new IntegerInterval("A", 20, true, 20, true); // age >= 20 && age <= 20

        IntegerInterval b = new IntegerInterval("B", 10, true, 20, true); // age >= 10 && age <= 20
        IntegerInterval c = new IntegerInterval("C", 10, true, 30, false); // age >= 10 && age < 30
        IntegerInterval d = new IntegerInterval("D", 0, true, 20, false); // age >= 0 && age < 20
        IntegerInterval e = new IntegerInterval("E", 20, true, 30, true); // age >= 20 && age <= 30

        index.addInterval(a);
        index.addInterval(b);
        index.addInterval(c);
        index.addInterval(d);
        index.addInterval(e);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b, c, d);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(b, c, d);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(a, b, c, e);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(c, e);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(40)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, b, c, d, e);

        index.removeInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b, d);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(b, d);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(a, b, e);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(40)).isEmpty();

        index.removeInterval(a);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b, d);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(b, d);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(b, e);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(40)).isEmpty();

        IntegerInterval f = new IntegerInterval("F", 15, true, 15, true); // age >= 15 && age <= 15
        index.addInterval(f);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(5)).containsExactlyInAnyOrder(d);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b, d);
        assertThat(index.getIntervals(15)).containsExactlyInAnyOrder(b, d, f);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(b, e);
        assertThat(index.getIntervals(25)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(30)).containsExactlyInAnyOrder(e);
        assertThat(index.getIntervals(40)).isEmpty();

        index.removeInterval(b);
        index.removeInterval(d);
        index.removeInterval(e);
        index.removeInterval(f);

        assertThat(index.getMapSize()).isEqualTo(0);
    }

    @Test
    public void testDuplicateInterval() {
        IntervalIndex<Integer, IntegerInterval> index = new IntervalIndex<>();

        // A and B are duplicate intervals but not the same instance/hash
        IntegerInterval a = new IntegerInterval("A", 0, true, 20, false); // age >= 0 && age < 20
        IntegerInterval b = new IntegerInterval("B", 0, true, 20, false); // age >= 0 && age < 20

        IntegerInterval c = new IntegerInterval("C", 0, true, 20, true); // age >= 0 && age <= 20
        index.addInterval(a);
        index.addInterval(b);
        index.addInterval(c);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(a, b, c);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(a, b, c);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(30)).isEmpty();

        assertThat(index.getAllIntervals()).containsExactlyInAnyOrder(a, b, c);

        index.removeInterval(a);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(b, c);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(b, c);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(30)).isEmpty();

        index.removeInterval(b);

        assertThat(index.getIntervals(-1)).isEmpty();
        assertThat(index.getIntervals(0)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(10)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(20)).containsExactlyInAnyOrder(c);
        assertThat(index.getIntervals(30)).isEmpty();

        index.removeInterval(c);

        assertThat(index.getMapSize()).isEqualTo(0);
    }

    private static class IntegerInterval implements Interval<Integer> {

        private String id;

        private Integer start;
        private boolean startInclusive;

        private Integer end;
        private boolean endInclusive;

        public IntegerInterval(String id, Integer start, boolean startInclusive, Integer end, boolean endInclusive) {
            super();
            this.id = id;
            this.start = start;
            this.startInclusive = startInclusive;
            this.end = end;
            this.endInclusive = endInclusive;
        }

        @Override
        public Integer getStart() {
            return start;
        }

        @Override
        public boolean isStartInclusive() {
            return startInclusive;
        }

        @Override
        public Integer getEnd() {
            return end;
        }

        @Override
        public boolean isEndInclusive() {
            return endInclusive;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            IntegerInterval other = (IntegerInterval) obj;
            if (id == null) {
                if (other.id != null)
                    return false;
            } else if (!id.equals(other.id))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return "IntegerInterval [id=" + id + ", start=" + start + ", startInclusive=" + startInclusive + ", end=" + end + ", endInclusive=" + endInclusive + "]";
        }

    }
}
