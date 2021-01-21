package com.dev.googlecloudtask

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File

class MainActivity : AppCompatActivity() {
    private var PICKFILE_RESULT_CODE = 1
    lateinit var selectFile: FloatingActionButton
    var fileUriNames = ArrayList<Uri>()
    lateinit var adapter: FileListAdapter
    lateinit var viewModel: CloudComputeViewModel
    private val requestCode = 0
    private var grantResults: IntArray = intArrayOf(-1, 0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED ) {
            ActivityCompat.requestPermissions(this, arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE) , requestCode)


            onRequestPermissionsResult(requestCode, arrayOf( Manifest.permission.WRITE_EXTERNAL_STORAGE ), grantResults)
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = FileListAdapter(ArrayList())
        recyclerView.adapter = adapter
        selectFile = findViewById(R.id.selectFileBtn)
        viewModel = ViewModelProvider(this).get(CloudComputeViewModel::class.java)
        selectFile.setOnClickListener {
            val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
            chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
            chooseFile.type = "text/plain"
            startActivityForResult(
                    Intent.createChooser(chooseFile, "Choose a file"),
                    PICKFILE_RESULT_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> {
                if (grantResults.isNotEmpty()
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.d("permission","granted");
                } else {

                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();

                    onDestroy();
                }
                return
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_submit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_submit -> {
                for (i in 0 until fileUriNames.size) {
                    viewModel.uploadFiles("cloud-task-301706", File(fileUriNames[i].path).toString(), this@MainActivity)
                }
                Toast.makeText(this, "Successfully uploaded to cloud", Toast.LENGTH_LONG).show()
            }
        }

        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK) {
            val content_describer = data?.data
            val source = File(content_describer?.path)
            Log.d("Dev src: ", source.toString())
            val filename = content_describer?.lastPathSegment
            fileUriNames.add(content_describer!!)
            adapter.data = fileUriNames
            adapter.notifyDataSetChanged()
            if (filename != null) {
                Log.d("FileName is ", filename)
            }
//            viewModel.uploadFiles("cloud-task-301706", source.toString(), this)
        }
    }
}