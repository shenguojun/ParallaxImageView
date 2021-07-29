package com.shengj.parallaximageview.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.shengj.parallaximageview.GravitySensor
import com.shengj.parallaximageview.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), GravitySensor.GravityListener {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Glide.with(binding.image1)
            .load(R.drawable.image1)
            .centerInside()
            .into(binding.image1)
        Glide.with(binding.image2)
            .load(R.drawable.image2)
            .centerInside()
            .into(binding.image2)
        Glide.with(binding.image3)
            .load(R.drawable.image3)
            .centerInside()
            .into(binding.image3)

        // 注册声明周期以及传感器回调
        lifecycle.addObserver(GravitySensor(this).also {
            it.listener = this
        })

    }

    override fun onGravityChange(x: Float, y: Float) {
        binding.image1.onGravityChange(x, y)
        binding.image3.onGravityChange(-x, -y)
    }

}