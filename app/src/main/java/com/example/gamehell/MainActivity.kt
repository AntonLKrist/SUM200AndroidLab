package com.example.gamehell

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.gamehell.composable.ShowGames
import com.example.gamehell.data.Games
import com.example.gamehell.ui.theme.GamehellTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val queue = Volley.newRequestQueue(application)
        enableEdgeToEdge()
        setContent {
            var games by remember { mutableStateOf<List<Games?>>(emptyList()) }

            val APIKEY = "6fb4f7c4b5d247ec809896fdec6f3af4"
            val url = "https://api.rawg.io/api/games?key=$APIKEY&page_size=100"


            val jsonRequest = JsonObjectRequest(
                Request.Method.GET,
                url,
                null, { response ->
                    val results = response.getJSONArray("results")
                    val gameList = mutableListOf<Games?>()

                    for(i in 0 until results.length()) {
                        val obj = results.getJSONObject(i)
                        val id = obj.getInt("id")
                        val name = obj.getString("name")
                        val rating = obj.optString("metacritic")
                        val imageUrl = obj.getString("background_image")
                        val released = obj.getString("released")

                        val game = Games(id, name, rating, imageUrl, released, "")
                        gameList.add(game)

                        val descriptionUrl = "https://api.rawg.io/api/games/$id?key=$APIKEY"
                        val detailsRequest = JsonObjectRequest(
                            Request.Method.GET,
                            descriptionUrl,
                            null,
                            { detailsResponse ->
                                val detailsText = detailsResponse.optString("description")
                                val updatedList = gameList.map{
                                    if(it?.id == id) it.copy(details = detailsText ) else it
                                }
                                games = updatedList
                            },
                            {error ->
                                Log.e("Volley error", error.toString())
                            }
                        )
                        queue.add(detailsRequest)

                    }
                    Log.d("API Response", response.toString())


                },
                {error ->
                    Log.e("Volley error", error.toString())
                }
            )
            queue.add(jsonRequest)


            GamehellTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ShowGames(
                        games = games as List<Games>,
                        modifier = Modifier.padding(innerPadding)

                    )
                }
            }
        }
    }
}






