package net.lightning.common;

import lombok.experimental.UtilityClass;
import net.lightning.api.database.model.ModelField;
import net.lightning.api.database.model.ModelManager;
import net.lightning.api.database.model.ModelTypeAccessor;
import net.lightning.common.models.types.RankTypeAccessor;
import net.lightning.common.models.types.SanctionTypeAccessor;

import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class CommonModelDeclaration {

    public void registerCustomAccessors() {
        Map<Class<?>, String> typeAccessorsNames = ModelManager.getTypeAccessorsNames();
        typeAccessorsNames.put(Rank.class, "rank");
        typeAccessorsNames.put(SanctionType.class, "sanction-type");

        Map<String, Function<ModelField, ModelTypeAccessor<?>>> typeAccessors = ModelManager.getTypeAccessors();
        typeAccessors.put("rank", RankTypeAccessor::new);
        typeAccessors.put("sanction-type", SanctionTypeAccessor::new);
    }

}
