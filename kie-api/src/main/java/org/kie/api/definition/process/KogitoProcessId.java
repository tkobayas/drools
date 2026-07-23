package org.kie.api.definition.process;

import java.lang.module.ModuleDescriptor.Version;
import java.util.Objects;
import java.util.Optional;


public class KogitoProcessId implements Comparable<KogitoProcessId> {


    private String id;
    private String version;
    private transient Optional<Version> versionObj;

    public static KogitoProcessId from (String id, String version) {
        return id == null ? null: new KogitoProcessId(id, version);
    }

    public KogitoProcessId(String id, String version) {
        this(Objects.requireNonNull(id), version, version == null ? Optional.empty() : parseVersion(version));
    }

    public KogitoProcessId(String id) {
        this(id, null);
    }
    
    private KogitoProcessId(String id, String version, Optional<Version> versionObj) {
        this.id = id;
        this.version = version;
        this.versionObj = versionObj;
    }

    public String id () {
    	return id;
    }

    public String version() {
        return version;
    }
    
    
    public String getProcessId() {
		return id;
	}
    
    public String getVersion() {
        return version;
    }
 
    @Override
    public boolean equals(Object o) {
    	return o instanceof KogitoProcessId id && compareTo(id) == 0;
    }
    
    @Override
    public int hashCode() {
    	return id.hashCode();
    }
    
    @Override
    public String toString() {
        return toString("_");
    }
    
    public String toString (String separator) {
        return version != null ? id+separator+version : id;
    }

    private static Optional<Version> parseVersion(String version) {
        try {
            return Optional.of(Version.parse(version));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    @Override
    public int compareTo(KogitoProcessId o) {
        int compare = id.compareTo(o.id);
        if (compare == 0 && versionObj.isPresent() && o.versionObj.isPresent()) {
            compare = versionObj.orElseThrow().compareTo(o.versionObj.orElseThrow());
        }
        if (compare == 0 && version != null && o.version != null) {
            compare = version.compareTo(o.version);
        }
        return compare;
    }
}
