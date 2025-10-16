package com.kk.cibaria.helper;

import com.kk.cibaria.exception.PageDoesNotExistException;
import com.kk.cibaria.recipe.Recipe;

import java.util.List;

public class Pagination {

    public List<Recipe> paginate(int page, int size, List<Recipe> recipes)
    {

        int itemsFrom = (page-1)*size;
        int itemsTo = Math.min(page*size,recipes.size());

        if(isPage(page,size,recipes))
        {
            return recipes.subList(itemsFrom, itemsTo);
        }else{
            throw new PageDoesNotExistException("Page " + page + " does not exists");
        }
    }

    private boolean isPage(int page, int size, List<Recipe> recipes)
    {
        int itemsFrom = (page - 1) * size;
        return itemsFrom < recipes.size() && page > 0;
    }

    public int getTotalPages(int size, List<Recipe> recipes) {
        return (int) Math.ceil((double) recipes.size() /size);
    }
}
