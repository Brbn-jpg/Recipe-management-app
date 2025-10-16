package com.kk.cibaria.step;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.kk.cibaria.recipe.Recipe;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Step {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 256, message = "Step content cannot exceed 256 characters")
    @Column(length = 256)
    private String content;

    @ManyToOne
    @JoinColumn(name = "recipeId")
    @JsonBackReference("recipe-steps")
    private Recipe recipe;

    public Step(String content, Recipe recipe){
        this.content=content;
        this.recipe = recipe;
    }
}
