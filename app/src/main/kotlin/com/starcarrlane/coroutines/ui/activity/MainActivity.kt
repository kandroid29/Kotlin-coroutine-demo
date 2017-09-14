/*
 * Copyright 2017 Miguel Castiblanco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.starcarrlane.coroutines.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.starcarrlane.coroutines.experimental.Android
import com.starcarrlane.coroutines.network.Post
import com.starcarrlane.coroutines.network.SampleClient
import com.starcarrlane.coroutines.ui.adapter.PostClickListener
import com.starcarrlane.coroutines.ui.adapter.PostsAdapter
import coroutines.R
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import okhttp3.Request
import java.io.IOException


/**
 * When resumed, will call the service to get the Posts and display them
 * Created by macastiblancot on 2/13/17.
 */
class MainActivity : AppCompatActivity(), PostClickListener {

    private lateinit var posts: RecyclerView
    private lateinit var postsAdapter: PostsAdapter
    private lateinit var postsLayoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        posts = findViewById(R.id.posts_list) as RecyclerView
        postsAdapter = PostsAdapter(listener = this)
        postsLayoutManager = LinearLayoutManager(this)

        posts.apply {
            setHasFixedSize(true)
            layoutManager = postsLayoutManager
            adapter = postsAdapter
        }
    }

    override fun onPostClicked(post: Post) {
        Toast.makeText(this, "Clicked ${post.id}", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()

        launch(Android) {
            try {
//                val result = SampleClient.fetchPosts()

                Log.d("coroutineTag", "BP1@Thread(${Thread.currentThread().id}, ${Thread.currentThread().name})")
                val result2 = async(CommonPool) {
                    Log.d("coroutineTag", "BP2@Thread(${Thread.currentThread().id}, ${Thread.currentThread().name})")
                    delay(500)
                    Log.d("coroutineTag", "BP3@Thread(${Thread.currentThread().id}, ${Thread.currentThread().name})")
                    val request = Request.Builder().url("https://jsonplaceholder.typicode.com/posts").build()
                    val response =  SampleClient.client.newCall(request).execute()
                    val postsType = object : TypeToken<List<Post>>() {}.type
                    Gson().fromJson<List<Post>>(response.body().string(), postsType)
                }

                Log.d("coroutineTag", "BP4@Thread(${Thread.currentThread().id}, ${Thread.currentThread().name})")
                val posts = result2.await()
                Log.d("coroutineTag", "BP5@Thread(${Thread.currentThread().id}, ${Thread.currentThread().name})")
                postsAdapter.setElements(posts) // will suspend until the call is finished
                postsAdapter.notifyDataSetChanged()
            } catch (exception: IOException){
                Toast.makeText(this@MainActivity, "Phone not connected or service down", Toast.LENGTH_SHORT).show()
            }

        }

        Log.d("coroutineTag", "BP6@Thread(${Thread.currentThread().id}, ${Thread.currentThread().name})")
    }
}
