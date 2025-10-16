import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  // vus: 5, // virtual users
  // duration: "30s",
  // iterations: 5,
  // duration: "2m",
  stages: [
    { duration: "30s", target: 2 },
    { duration: "1m", target: 15 },
    { duration: "2m", target: 10 },
    { duration: "30s", target: 0 },
  ],
  noConnectionReuse: true,
  discardResponseBodies: false,
};

function generateTestUser() {
  const timestamp = Date.now().toString().slice(-6);
  const random = Math.floor(Math.random() * 1000);
  const unique = `${timestamp}${random}`;
  return {
    username: `k6test_${unique}`,
    email: `k6test_${unique}@test.com`,
    password: "TestPassword123!",
  };
}

function createMultipartRequest(data, token) {
  // no idea how this works (its caused by backend requiring MULTIPART_FORM_DATA_VALUE instead of json)
  // well just have to deal with it if recipes has to have an image
  const boundary =
    "----WebKitFormBoundary" + Math.random().toString(36).substr(2);

  const body =
    `------${boundary}\r\n` +
    `Content-Disposition: form-data; name="recipe"\r\n\r\n` +
    `${JSON.stringify(data)}\r\n` +
    `------${boundary}--\r\n`;

  return {
    body: body,
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": `multipart/form-data; boundary=----${boundary}`,
    },
  };
}

export function setup() {
  const BASE_URL = "http://localhost:8080/api";

  const healthCheck = http.get(`${BASE_URL}/recipes`);
  if (healthCheck.status !== 200) {
    throw new Error("Backend is not responding");
  }

  return {
    baseUrl: BASE_URL,
  };
}

export default function (data) {
  const testUser = generateTestUser();
  const newEmail = `${testUser.username}_new@test.com`;

  const registerResponse = http.post(
    `${data.baseUrl}/register`,
    JSON.stringify(testUser),
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );
  console.log(`Created user: ${testUser.email}`);

  if (registerResponse.status === 400) {
    throw new Error(`Registration validation failed: ${registerResponse.body}`);
  }

  if (registerResponse.status !== 200 && registerResponse.status !== 201) {
    throw new Error(
      `Registration failed: ${registerResponse.status}, ${registerResponse.body}`
    );
  }

  sleep(2);

  const loginResponse = http.post(
    `${data.baseUrl}/authenticate`,
    JSON.stringify({
      email: testUser.email,
      password: testUser.password,
    }),
    {
      headers: {
        "Content-Type": "application/json",
      },
    }
  );

  if (loginResponse.status !== 200) {
    throw new Error(`Login failed after registration`);
  }

  const responseData = loginResponse.json();
  const token = responseData.token;
  const authHeaders = {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  };

  const profileResponse = http.get(`${data.baseUrl}/users/aboutme`, {
    headers: {
      Authorization: `Bearer ${token}`,
      "Content-Type": "application/json",
    },
  });

  if (profileResponse.status !== 200) {
    throw new Error(`Could not get user profile`);
  }

  const userId = profileResponse.json().id;

  sleep(2);
  testPublicEndpoints(data.baseUrl);
  testAuthenticatedEndpoints({
    baseUrl: data.baseUrl,
    token: token,
    testUser: testUser,
    newEmail: newEmail,
    userId: userId,
    authHeaders: authHeaders,
  });
  sleep(1);
  http.del(`${data.baseUrl}/users/${userId}`, null, { headers: authHeaders });
  console.log(`Deleted user ${userId}`);
}

function testPublicEndpoints(baseUrl) {
  //================================================
  // GET /recipes - all recipes
  //================================================
  let recipesResponse = http.get(`${baseUrl}/recipes`);
  check(recipesResponse, {
    "GET /recipes = status 200": (r) => r.status === 200,
    "GET /recipes = has content": (r) => r.body.length > 0,
    "GET /recipes = response time < 1000ms": (r) => r.timings.duration < 1000,
  });

  //================================================
  // GET /recipes/{id} - specified recipe
  //================================================
  let recipeResponse = http.get(`${baseUrl}/recipes/1`);
  check(recipeResponse, {
    "GET /recipes/1 = status 200 or 404": (r) =>
      r.status === 200 || r.status === 404,
  });

  //================================================
  // GET /recipes with filters
  //================================================
  let filteredRecipesResponse = http.get(
    `${baseUrl}/recipes?category=DINNER&difficulty=2`
  );
  check(filteredRecipesResponse, {
    "GET /recipes with filters = status 200": (r) => r.status === 200,
  });

  //================================================
  // GET /recipes/search - searching
  //================================================
  let searchResponse = http.get(`${baseUrl}/recipes/search?query=chicken`);
  check(searchResponse, {
    "Search recipes = status 200": (r) => r.status === 200,
  });
}

function testAuthenticatedEndpoints(data) {
  //================================================
  // GET /users/aboutme - user profile
  //================================================
  let profileResponse = http.get(`${data.baseUrl}/users/aboutme`, {
    headers: data.authHeaders,
  });
  check(profileResponse, {
    "GET /users/aboutme = status 200": (r) => r.status === 200,
  });

  //================================================
  // POST /recipes
  //================================================
  const random = Math.floor(Math.random() * 10000);
  const newRecipe = {
    recipeName: `Test Recipe ${random}`,
    difficulty: "1",
    ingredients: [
      {
        ingredientName: "test ingredient",
        quantity: 1,
        unit: "pcs",
        isOptional: false,
      },
    ],
    prepareTime: 15,
    servings: 2,
    category: "DINNER",
    isPublic: true,
    steps: [
      { content: "Step 1: Do something" },
      { content: "Step 2: Do something else" },
    ],
    language: "polish",
  };

  const request = createMultipartRequest(newRecipe, data.token);
  let postedRecipe = http.post(`${data.baseUrl}/recipes`, request.body, {
    headers: request.headers,
  });

  let recipeId = null;
  if (postedRecipe.status === 200 && postedRecipe.body) {
    recipeId = postedRecipe.json().id;
  }

  check(postedRecipe, {
    "POST /recipes/ = status 200": (r) => r.status === 200,
  });

  sleep(2);

  //================================================
  // PUT /recipes/{id}
  //================================================
  const updatedRecipe = {
    recipeName: `Test Recipe ${random}`,
    difficulty: "2",
    ingredients: [
      {
        ingredientName: "test ingredient",
        quantity: 1,
        unit: "pcs",
        isOptional: false,
      },
    ],
    prepareTime: 15,
    servings: 2,
    category: "BREAKFAST",
    isPublic: true,
    steps: [{ content: "Do something" }, { content: "Do something else" }],
    language: "english",
  };
  const updateRequest = createMultipartRequest(updatedRecipe, data.token);

  let updateRecipeResponse = http.put(
    `${data.baseUrl}/recipes/${recipeId}`,
    updateRequest.body,
    {
      headers: updateRequest.headers,
    }
  );

  check(updateRecipeResponse, {
    "PUT /recipes/{id} = status 200": (r) => r.status === 200,
  });

  sleep(2);

  //================================================
  // GET /users/recipes - user recipes
  //================================================
  let myRecipesResponse = http.get(`${data.baseUrl}/users/recipes`, {
    headers: data.authHeaders,
  });
  check(myRecipesResponse, {
    "GET /users/recipes = status 200": (r) => r.status === 200,
  });

  //================================================
  // Favourite recipes tests
  //================================================

  // add to fav
  let favouritesAddResponse = http.post(
    `${data.baseUrl}/recipes/favourites/add`,
    JSON.stringify({ recipeId: recipeId }),
    { headers: data.authHeaders }
  );
  check(favouritesAddResponse, {
    "POST /recipes/favourites/add = status 200": (r) => r.status === 200,
  });
  sleep(2);

  // check if fav
  let isFavouriteResponse = http.get(
    `${data.baseUrl}/recipes/favourites/isFavourite?recipeId=${recipeId}`,
    { headers: data.authHeaders }
  );
  check(isFavouriteResponse, {
    "GET /recipes/favourites/isFavourite = status 200": (r) => r.status === 200,
  });
  sleep(2);

  // get all favs
  let userFavouritesResponse = http.get(`${data.baseUrl}/users/favourites`, {
    headers: data.authHeaders,
  });
  check(userFavouritesResponse, {
    "GET /users/favourites = status 200": (r) => r.status === 200,
  });
  sleep(2);

  // remove form favs
  if (favouritesAddResponse.status === 200) {
    let favouritesDeleteResponse = http.post(
      `${data.baseUrl}/recipes/favourites/delete`,
      JSON.stringify({ recipeId: recipeId }),
      { headers: data.authHeaders }
    );
    check(favouritesDeleteResponse, {
      "POST /recipes/favourites/delete = status 200": (r) => r.status === 200,
    });
    sleep(2);
  }

  //================================================
  // PUT /users/{id}/profile update username/description
  //================================================
  const profileUpdate = {
    id: data.userId,
    photoUrl: null,
    backgroundUrl: null,
    username: data.testUser.username + "updated",
    description: "Test user description updated",
    favourites: [],
    UserRecipes: [],
  };

  let updateUserProfile = http.put(
    `${data.baseUrl}/users/${data.userId}/profile`,
    JSON.stringify(profileUpdate),
    { headers: data.authHeaders }
  );

  check(updateUserProfile, {
    "PUT /users/{id}/profile = status 200": (r) => r.status === 200,
  });
  sleep(2);

  //================================================
  // PUT /users/{id}/email - email change
  //================================================
  let updateUserEmail = http.put(
    `${data.baseUrl}/users/${data.userId}/email`,
    JSON.stringify({
      newEmail: data.newEmail,
      password: data.testUser.password,
    }),
    { headers: data.authHeaders }
  );

  check(updateUserEmail, {
    "PUT /users/{id}/email = status 200": (r) => r.status === 200,
  });

  if (updateUserEmail.status === 200) {
    const reLoginResponse = http.post(
      `${data.baseUrl}/authenticate`,
      JSON.stringify({
        email: data.newEmail,
        password: data.testUser.password,
      }),
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (reLoginResponse.status === 200) {
      const newToken = reLoginResponse.json().token;
      data.authHeaders.Authorization = `Bearer ${newToken}`;
    }
  }
  sleep(2);

  //================================================
  // PUT /users/{id}/password - password change
  //================================================

  const newPassword = "NewTestPassword456!";
  let updateUserPassword = http.put(
    `${data.baseUrl}/users/${data.userId}/password`,
    JSON.stringify({
      currentPassword: data.testUser.password,
      newPassword: newPassword,
    }),
    { headers: data.authHeaders }
  );

  check(updateUserPassword, {
    "PUT /users/{id}/password = status 200": (r) => r.status === 200,
  });

  if (updateUserPassword.status === 200) {
    const reLoginResponse = http.post(
      `${data.baseUrl}/authenticate`,
      JSON.stringify({
        email: data.newEmail,
        password: newPassword,
      }),
      {
        headers: {
          "Content-Type": "application/json",
        },
      }
    );
    if (reLoginResponse.status === 200) {
      const newToken = reLoginResponse.json().token;
      data.authHeaders.Authorization = `Bearer ${newToken}`;
    }
  }
  sleep(2);

  //================================================
  // Rating recipe test
  //================================================
  if (recipeId) {
    // GET /recipes/{id}/rating
    let ratingResponse = http.get(
      `${data.baseUrl}/recipes/${recipeId}/rating`,
      { headers: data.authHeaders }
    );
    check(ratingResponse, {
      "GET /recipes/{id}/rating = status 200": (r) => r.status === 200,
    });

    sleep(2);

    // POST /recipes/{id}/rating
    let randomRating = Math.floor((Math.random() * 10) / 2) + 1; // from 1 to 5
    let postRating = http.post(
      `${data.baseUrl}/recipes/${recipeId}`,
      JSON.stringify(randomRating),
      {
        headers: data.authHeaders,
      }
    );
    check(postRating, {
      "POST /recipes/{id} = status 200": (r) => r.status === 200,
    });
    sleep(2);

    // GET /recipes/{id}/isOwner
    let isOwnerResponse = http.get(
      `${data.baseUrl}/recipes/${recipeId}/isOwner`,
      { headers: data.authHeaders }
    );
    check(isOwnerResponse, {
      "GET /recipes/{id}/isOwner = status 200": (r) => r.status === 200,
    });
    sleep(2);
  }

  //================================================
  // DELETE /recipe/{id} - delete recipe
  //================================================

  if (recipeId) {
    let deleteResponse = http.del(`${data.baseUrl}/recipes/${recipeId}`, null, {
      headers: data.authHeaders,
    });
    console.log("deleted recipe with id: " + recipeId);
    check(deleteResponse, {
      "DELETE /recipes/{id} = status 200": (r) => r.status === 200,
    });
  }
}
