package net.lightning.api.database.model;

import java.io.Serializable;

public interface CachableModel extends Serializable {
    
    String getCacheKey();

}
