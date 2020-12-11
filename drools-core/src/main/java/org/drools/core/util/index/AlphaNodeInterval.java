/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import org.drools.core.reteoo.AlphaNode;

/**
 * 
 * A pair of AlphaNodes which composes an interval. e.g. Person(age >= 10 && < 20)
 *
 */
public class AlphaNodeInterval implements Interval<Comparable> {

    private AlphaNode formerNode;
    private AlphaNode latterNode;

    private Comparable<?> start;
    private boolean startInclusive;

    private Comparable<?> end;
    private boolean endInclusive;

    public AlphaNodeInterval(AlphaNode formerNode, AlphaNode latterNode) {
        this.formerNode = formerNode;
        this.latterNode = latterNode;
        // TODO: init fields
    }

    public AlphaNode getFormerNode() {
        return formerNode;
    }

    public AlphaNode getLatterNode() {
        return latterNode;
    }

    public Comparable<?> getStart() {
        return start;
    }

    public boolean isStartInclusive() {
        return startInclusive;
    }

    public Comparable<?> getEnd() {
        return end;
    }

    public boolean isEndInclusive() {
        return endInclusive;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((formerNode == null) ? 0 : formerNode.hashCode());
        result = prime * result + ((latterNode == null) ? 0 : latterNode.hashCode());
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
        AlphaNodeInterval other = (AlphaNodeInterval) obj;
        if (formerNode == null) {
            if (other.formerNode != null)
                return false;
        } else if (!formerNode.equals(other.formerNode))
            return false;
        if (latterNode == null) {
            if (other.latterNode != null)
                return false;
        } else if (!latterNode.equals(other.latterNode))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "AlphaNodeInterval [formerNode=" + formerNode + ", latterNode=" + latterNode + ", start=" + start + ", startInclusive=" + startInclusive + ", end=" + end + ", endInclusive=" + endInclusive + "]";
    }

}
