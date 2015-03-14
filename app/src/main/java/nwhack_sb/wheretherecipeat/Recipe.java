package nwhack_sb.wheretherecipeat;

import java.util.List;

/**
 * Created by William on 2015-03-14.
 */
public class Recipe {

    private String recipeName;
    private String recipeId;
    private String recipeURL;
    private String cuisine;
    private String cookingMethod;
    private List<String> ingredients;
    private String imageFullURL;
    private String imageThumbURL;
    // TODO: find Android image type
    //imageFull;
    //imageThumb;
    private RecipeDetails recipeDetails;

    public Recipe(String recipeName, String recipeId, String recipeURL, String cuisine, String cookingMethod, List<String> ingredients, String imageFullURL, String imageThumbURL, RecipeDetails recipeDetails) {
        this.recipeName = recipeName;
        this.recipeId = recipeId;
        this.recipeURL = recipeURL;
        this.cuisine = cuisine;
        this.cookingMethod = cookingMethod;
        this.ingredients = ingredients;
        this.imageFullURL = imageFullURL;
        this.imageThumbURL = imageThumbURL;
        this.recipeDetails = recipeDetails;
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public String getCookingMethod() {
        return cookingMethod;
    }

    public String getImageFullURL() {
        return imageFullURL;
    }

    public String getImageThumbURL() {
        return imageThumbURL;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getName() {
        return recipeName;
    }

    public String getRecipeURL() {
        return recipeURL;
    }

    public RecipeDetails getRecipeDetails() {
        return recipeDetails;
    }
}
