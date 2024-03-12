package io.github.no.today.watchdog

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.github.no.today.watchdog.databinding.ActivityMainBinding
import io.github.no.today.watchdog.socket.SocketClient
import io.github.no.today.watchdog.socket.SocketServer

class MainActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.longText.setOnClickListener(this)

        SocketServer(20300).asyncStart()
        SocketClient("127.0.0.1", 20300).asyncStart()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.longText -> {
                Toast.makeText(applicationContext, "Click", Toast.LENGTH_SHORT).show()
            }
        }
    }
}