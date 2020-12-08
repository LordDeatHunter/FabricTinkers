package wraith.smithee.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import wraith.smithee.Config;
import wraith.smithee.properties.*;
import wraith.smithee.registry.ItemRegistry;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JsonParser {

    public static void parseIndividualPart(JsonObject part, Properties properties, String type) {
        int mineLevel = part.get("mining_level").getAsInt();
        int durability = part.get("durability").getAsInt();
        float mineSpeed = part.get("mining_speed").getAsFloat();
        float attackDamage = part.get("attack_damage").getAsFloat();
        float attackSpeed = part.get("attack_speed").getAsFloat();

        properties.partProperties.put(type, new Property(mineSpeed, mineLevel, durability, attackDamage, attackSpeed));
        if (!part.has("traits")) {
            return;
        }
        for (JsonElement trait : part.get("traits").getAsJsonArray()) {
            JsonObject traitObject = trait.getAsJsonObject();
            String traitName = traitObject.get("trait").getAsString();
            int minLevel = traitObject.get("min_level").getAsInt();
            int maxLevel = traitObject.get("max_level").getAsInt();
            double chance = traitObject.get("chance").getAsDouble();
            properties.traits.get(type).add(new Trait(traitName, minLevel, maxLevel, chance));
        }

    }

    public static void parseRecipes(Set<Map.Entry<String, JsonElement>> recipes, HashMap<Item, HashMap<String, ToolPartRecipe>> recipeList, HashMap<String, HashMap<Item, Integer>> remains) {
        for (Map.Entry<String, JsonElement> entry : recipes) {
            JsonObject recipe = entry.getValue().getAsJsonObject();
            String material = entry.getKey();
            String outputMaterial = recipe.get("output_material").getAsString();
            int chiselingLevel = recipe.get("chiseling_level").getAsInt();
            int worth = recipe.get("material_value").getAsInt();

            HashSet<Item> items = new HashSet<>();
            if (material.startsWith("#")) {
                items.addAll(TagRegistry.item(new Identifier(material.substring(1))).values());
            } else {
                items.add(Registry.ITEM.get(new Identifier(material)));
            }
            JsonObject overrides = recipe.get("overrides").getAsJsonObject();
            for (Item item : items) {
                if (!remains.containsKey(outputMaterial)) {
                    remains.put(outputMaterial, new HashMap<>());
                }
                remains.get(outputMaterial).put(item, worth);
                recipeList.put(item, new HashMap<>());
                for (String recipeType : ItemRegistry.BASE_RECIPE_VALUES.keySet()) {
                    int base = ItemRegistry.BASE_RECIPE_VALUES.get(recipeType);
                    recipeList.get(item).put(recipeType, new ToolPartRecipe(outputMaterial, base, chiselingLevel));
                    if (overrides.has("all")) {
                        recipeList.get(item).get(recipeType).requiredAmount = (int) Utils.evaluateExpression(overrides.get("all").getAsString().replace("base", String.valueOf(base)));
                    } else if (overrides.has(recipeType)) {
                        recipeList.get(item).get(recipeType).requiredAmount = (int) Utils.evaluateExpression(overrides.get(recipeType).getAsString().replace("base", String.valueOf(base)));
                    }
                }
            }
        }
    }

    public static void parseCombinations() {
        File[] files = Config.getFiles("config/smithee/combinations/");
        for (File file : files){
            JsonObject json = Config.getJsonObject(Config.readFile(file));
            for (JsonElement combination : json.get("combinations").getAsJsonArray()) {

                HashMap<String, HashSet<String>> includes = new HashMap<>();
                includes.put("head", new HashSet<>());
                includes.put("binding", new HashSet<>());
                includes.put("handle", new HashSet<>());

                for (JsonElement element : combination.getAsJsonObject().get("includes").getAsJsonArray()) {
                    String[] segments = element.getAsString().split("_");
                    String material = "";
                    for (int i = 0; i < segments.length - 1; ++i) {
                        material += segments[i];
                    }
                    includes.get(segments[segments.length - 1]).add(material);
                }

                HashMap<String, HashSet<String>> excludes = new HashMap<>();
                excludes.put("head", new HashSet<>());
                excludes.put("binding", new HashSet<>());
                excludes.put("handle", new HashSet<>());

                for (JsonElement element : combination.getAsJsonObject().get("excludes").getAsJsonArray()) {
                    String[] segments = element.getAsString().split("_");
                    String material = "";
                    for (int i = 0; i < segments.length - 1; ++i) {
                        material += segments[i];
                    }
                    excludes.get(segments[segments.length - 1]).add(material);
                }

                HashMap<String, String> properties = new HashMap<>();
                JsonObject obj = combination.getAsJsonObject();

                if (obj.has("miningSpeed")) {
                    String miningSpeed = obj.get("mining_speed").getAsString();
                    properties.put("mining_speed", miningSpeed);
                } else if (obj.has("miningLevel")) {
                    String miningLevel = obj.get("mining_level").getAsString();
                    properties.put("mining_level", miningLevel);
                } else if (obj.has("durability")) {
                    String durability = obj.get("durability").getAsString();
                    properties.put("durability", durability);
                } else if (obj.has("attackDamage")) {
                    String attackDamage = obj.get("attack_damage").getAsString();
                    properties.put("attack_damage", attackDamage);
                } else if (obj.has("attackSpeed")) {
                    String attackSpeed = obj.get("attack_speed").getAsString();
                    properties.put("attack_speed", attackSpeed);
                }
                PartCombination.COMBINATIONS.add(new PartCombination(includes, excludes, properties));
            }
        }
    }

}